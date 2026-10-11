package app.clearscanner.patches.backup

import app.clearscanner.patches.shared.Constants.COMPATIBILITY_CLEAR_SCANNER
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch

private const val EXTENSION_CLASS = "Lapp/clearscanner/extension/AutoBackup;"
private const val BACKUP_ACTIVITY = "Lcom/indymobile/app/activity/MyBackupActivity;"
private const val MAIN_ACTIVITY = "Lcom/indymobile/app/activity/MainActivity;"

@Suppress("unused")
val autoBackupPatch = bytecodePatch(
    name = "Local auto backup",
    description = "Replaces cloud sync with a local backup. The Sync button opens the backup screen the " +
        "first time. After a backup file has been chosen, Sync overwrites that file automatically.",
    default = true
) {
    compatibleWith(COMPATIBILITY_CLEAR_SCANNER)

    extendWith("extensions/extension.mpe")

    execute {
        // 1. "Create a backup" button: before the file picker opens (after the app's own checks),
        //    write to the remembered file if there is one. Index is right after Intent.<init>.
        BackupButtonClickFingerprint.let {
            val afterIntentInit = it.instructionMatches[1].index + 1
            it.method.addInstructionsWithLabels(
                afterIntentInit,
                """
                    invoke-static {v5}, $EXTENSION_CLASS->openStream(Landroid/app/Activity;)Ljava/io/OutputStream;
                    move-result-object v3
                    if-eqz v3, :manual
                    invoke-virtual {v5, v3}, $BACKUP_ACTIVITY->O(Ljava/io/OutputStream;)V
                    return-void
                    :manual
                    nop
                """
            )
        }

        // 2. Remember the file the user picked, right before the backup is written to it.
        BackupFileChosenFingerprint.let {
            val beforeBackupWrite = it.instructionMatches[0].index
            it.method.addInstructions(
                beforeBackupWrite,
                """
                    iget-object v0, p0, $BACKUP_ACTIVITY->m:Landroid/net/Uri;
                    invoke-static {p0, v0}, $EXTENSION_CLASS->rememberFile(Landroid/content/Context;Landroid/net/Uri;)V
                """
            )
        }

        // 3. Backup screen created: press "Create a backup" automatically when launched by Sync.
        BackupScreenCreateFingerprint.let {
            val afterSecondButton = it.instructionMatches[1].index + 1
            it.method.addInstructions(
                afterSecondButton,
                "invoke-static {p0}, $EXTENSION_CLASS->onBackupScreenCreated(Landroid/app/Activity;)V"
            )
        }

        // 4. Sync screen -> backup screen.
        OpenSyncScreenFingerprint.method.addInstructions(
            0,
            """
                new-instance v0, Landroid/content/Intent;
                const-class v1, $BACKUP_ACTIVITY
                invoke-direct {v0, p0, v1}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
                invoke-static {p0, v0}, $EXTENSION_CLASS->prepareIntent(Landroid/content/Context;Landroid/content/Intent;)V
                invoke-virtual {p0, v0}, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V
                return-void
            """
        )

        // 5. "Sync now" always goes to the same place.
        SyncNowFingerprint.method.addInstructions(
            0,
            """
                invoke-virtual {p0}, $MAIN_ACTIVITY->T()V
                return-void
            """
        )
    }
}
