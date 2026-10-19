/*
 * Copyright (C) 2026 hxreborn
 * SPDX-License-Identifier: GPL-3.0-only
 */
package app.morphe.patches.musicolet.misc.pairip

import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patches.shared.compat.AppCompatibilities
import app.morphe.patches.shared.misc.generated.addDefaultConstructor
import app.morphe.patches.shared.misc.generated.replaceMethod
import app.morphe.patches.shared.misc.pairip.removePairipVirtualizationPatch
import com.android.tools.smali.dexlib2.iface.Method
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val VM_RUNNER = "Lcom/pairip/VMRunner;"
private const val APP_PACKAGE = "Lin/krosbits/musicolet"
private const val DO_IN_BACKGROUND = "doInBackground"
private const val ON_RECEIVE = "onReceive"

private class RestoredMethod(val type: String, val name: String, val registers: Int, val smali: String)

private val RESTORED_METHODS = listOf(
    RestoredMethod(
        "La61;", DO_IN_BACKGROUND, 4,
        """
            const/4 v0, 0x1
            iput-boolean v0, p0, La61;->c:Z
            :drain
            iget-object v0, p0, La61;->a:Ljava/util/ArrayList;
            invoke-virtual {v0}, Ljava/util/ArrayList;->isEmpty()Z
            move-result v1
            if-nez v1, :drained
            const/4 v1, 0x0
            invoke-virtual {v0, v1}, Ljava/util/ArrayList;->remove(I)Ljava/lang/Object;
            move-result-object v0
            check-cast v0, Lz51;
            iput-object v0, p0, La61;->b:Lz51;
            :run_start
            invoke-interface {v0}, Ljava/lang/Runnable;->run()V
            :run_end
            .catch Ljava/lang/Throwable; {:run_start .. :run_end} :run_failed
            :ran
            const/4 v0, 0x0
            iput-object v0, p0, La61;->b:Lz51;
            goto :drain
            :run_failed
            move-exception v1
            invoke-virtual {v1}, Ljava/lang/Throwable;->printStackTrace()V
            goto :ran
            :drained
            const/4 v0, 0x0
            iput-boolean v0, p0, La61;->c:Z
            return-object v0
        """,
    ),
    RestoredMethod(
        "Lon1;", DO_IN_BACKGROUND, 7,
        """
            iget-object v0, p0, Lon1;->c:Lqn1;
            iget-object v1, p0, Lon1;->b:Ldm0;
            iget-wide v2, p0, Lon1;->a:J
            const/4 v4, 0x1
            invoke-virtual {v0, v1, v2, v3, v4}, Lqn1;->u(Ldm0;JZ)V
            const/4 v0, 0x0
            return-object v0
        """,
    ),
    RestoredMethod(
        "Lnn1;", DO_IN_BACKGROUND, 7,
        """
            :start
            iget-object v0, p0, Lnn1;->g:Lqn1;
            invoke-virtual {v0}, Lqn1;->getWritableDatabase()Landroid/database/sqlite/SQLiteDatabase;
            move-result-object v0
            new-instance v1, Landroid/content/ContentValues;
            const/4 v2, 0x5
            invoke-direct {v1, v2}, Landroid/content/ContentValues;-><init>(I)V
            iget-wide v2, p0, Lnn1;->a:J
            invoke-static {v2, v3}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
            move-result-object v2
            const-string v3, "COL_LAST_PLAYED"
            invoke-virtual {v1, v3, v2}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Long;)V
            iget v2, p0, Lnn1;->b:I
            invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v2
            const-string v3, "COL_NUM_PLAYED"
            invoke-virtual {v1, v3, v2}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
            iget v2, p0, Lnn1;->c:I
            invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v2
            const-string v3, "COL_NUM_PLAYED_W"
            invoke-virtual {v1, v3, v2}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
            iget v2, p0, Lnn1;->d:I
            invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v2
            const-string v3, "COL_NUM_PLAYED_M"
            invoke-virtual {v1, v3, v2}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
            iget v2, p0, Lnn1;->e:I
            invoke-static {v2}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v2
            const-string v3, "COL_NUM_PLAYED_Y"
            invoke-virtual {v1, v3, v2}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
            iget-object v2, p0, Lnn1;->f:Ldm0;
            iget-object v2, v2, Ldm0;->c:Lkn1;
            iget-object v2, v2, Lkn1;->f:Lro;
            invoke-virtual {v2}, Lro;->toString()Ljava/lang/String;
            move-result-object v2
            const/4 v3, 0x1
            new-array v3, v3, [Ljava/lang/String;
            const/4 v4, 0x0
            aput-object v2, v3, v4
            const-string v2, "TABLE_SONGS"
            const-string v4, "COL_PATH=?"
            invoke-virtual {v0, v2, v1, v4, v3}, Landroid/database/sqlite/SQLiteDatabase;->update(Ljava/lang/String;Landroid/content/ContentValues;Ljava/lang/String;[Ljava/lang/String;)I
            :end
            .catch Ljava/lang/Throwable; {:start .. :end} :failed
            :done
            const/4 v0, 0x0
            return-object v0
            :failed
            move-exception v0
            invoke-virtual {v0}, Ljava/lang/Throwable;->printStackTrace()V
            goto :done
        """,
    ),
    RestoredMethod(
        "Lht0;", DO_IN_BACKGROUND, 4,
        """
            iget v0, p0, Lht0;->a:I
            if-nez v0, :save_favorites
            sput-object p0, $APP_PACKAGE/MediaMountedReceiver;->b:Lht0;
            :start
            invoke-virtual {p0}, Landroid/os/AsyncTask;->isCancelled()Z
            move-result v0
            if-nez v0, :done
            const-wide/16 v0, 0x1388
            invoke-static {v0, v1}, Ljava/lang/Thread;->sleep(J)V
            invoke-virtual {p0}, Landroid/os/AsyncTask;->isCancelled()Z
            move-result v0
            if-nez v0, :done
            invoke-static {}, $APP_PACKAGE/MyApplication;->q()Z
            move-result v0
            if-nez v0, :done
            invoke-virtual {p0}, Landroid/os/AsyncTask;->isCancelled()Z
            move-result v0
            if-nez v0, :done
            const-wide/16 v0, 0xfa0
            invoke-static {v0, v1}, Ljava/lang/Thread;->sleep(J)V
            invoke-virtual {p0}, Landroid/os/AsyncTask;->isCancelled()Z
            move-result v0
            if-nez v0, :done
            invoke-static {}, $APP_PACKAGE/MyApplication;->q()Z
            :end
            .catch Ljava/lang/Throwable; {:start .. :end} :failed
            :done
            const/4 v0, 0x0
            sput-object v0, $APP_PACKAGE/MediaMountedReceiver;->b:Lht0;
            return-object v0
            :failed
            move-exception v0
            invoke-virtual {v0}, Ljava/lang/Throwable;->printStackTrace()V
            goto :done
            :save_favorites
            invoke-static {}, Ls71;->i()V
            const/4 v0, 0x0
            return-object v0
        """,
    ),
    RestoredMethod(
        "Lkb;", DO_IN_BACKGROUND, 14,
        """
            iget v0, p0, Lkb;->a:I
            if-eqz v0, :backup
            iget-object v0, p0, Lkb;->e:Ljava/lang/Object;
            check-cast v0, Ljava/util/List;
            const/4 v1, 0x0
            :clear_start
            sget-object v2, $APP_PACKAGE/MyApplication;->j:Lqn1;
            invoke-virtual {v2}, Lqn1;->getWritableDatabase()Landroid/database/sqlite/SQLiteDatabase;
            move-result-object v1
            invoke-virtual {v1}, Landroid/database/sqlite/SQLiteDatabase;->beginTransactionNonExclusive()V
            new-instance v2, Landroid/content/ContentValues;
            const/4 v3, 0x7
            invoke-direct {v2, v3}, Landroid/content/ContentValues;-><init>(I)V
            const/4 v3, 0x0
            invoke-static {v3}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object v3
            const-string v4, "COL_NUM_PLAYED"
            invoke-virtual {v2, v4, v3}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
            const-string v4, "COL_NUM_PLAYED_W"
            invoke-virtual {v2, v4, v3}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
            const-string v4, "COL_NUM_PLAYED_M"
            invoke-virtual {v2, v4, v3}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
            const-string v4, "COL_NUM_PLAYED_Y"
            invoke-virtual {v2, v4, v3}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
            const-string v4, "COL_LAST_PLAYED"
            invoke-virtual {v2, v4, v3}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
            const-string v4, "COL_LASTPOS"
            invoke-virtual {v2, v4, v3}, Landroid/content/ContentValues;->put(Ljava/lang/String;Ljava/lang/Integer;)V
            const/4 v4, 0x1
            new-array v4, v4, [Ljava/lang/String;
            invoke-interface {v0}, Ljava/util/List;->iterator()Ljava/util/Iterator;
            move-result-object v5
            :update_next
            invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z
            move-result v6
            if-eqz v6, :updated
            invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;
            move-result-object v6
            check-cast v6, Ldm0;
            const-wide/16 v9, 0x0
            iget-wide v7, v6, Ldm0;->m:J
            cmp-long v11, v7, v9
            if-nez v11, :update_song
            iget-wide v7, v6, Ldm0;->n:J
            cmp-long v11, v7, v9
            if-eqz v11, :update_next
            :update_song
            iget-object v7, v6, Ldm0;->c:Lkn1;
            iget-object v7, v7, Lkn1;->f:Lro;
            invoke-virtual {v7}, Lro;->toString()Ljava/lang/String;
            move-result-object v7
            const/4 v8, 0x0
            aput-object v7, v4, v8
            const-string v7, "TABLE_SONGS"
            const-string v8, "COL_PATH=?"
            invoke-virtual {v1, v7, v2, v8, v4}, Landroid/database/sqlite/SQLiteDatabase;->update(Ljava/lang/String;Landroid/content/ContentValues;Ljava/lang/String;[Ljava/lang/String;)I
            goto :update_next
            :updated
            invoke-virtual {v1}, Landroid/database/sqlite/SQLiteDatabase;->setTransactionSuccessful()V
            invoke-static {v0}, Ld51;->b(Ljava/util/List;)V
            invoke-interface {v0}, Ljava/util/List;->iterator()Ljava/util/Iterator;
            move-result-object v5
            :reset_next
            invoke-interface {v5}, Ljava/util/Iterator;->hasNext()Z
            move-result v6
            if-eqz v6, :clear_end
            invoke-interface {v5}, Ljava/util/Iterator;->next()Ljava/lang/Object;
            move-result-object v6
            check-cast v6, Ldm0;
            const/4 v7, 0x0
            iput v7, v6, Ldm0;->t:I
            iput v7, v6, Ldm0;->r:I
            iput v7, v6, Ldm0;->s:I
            iput v7, v6, Ldm0;->o:I
            const-wide/16 v7, 0x0
            iput-wide v7, v6, Ldm0;->m:J
            iput-wide v7, v6, Ldm0;->n:J
            goto :reset_next
            :clear_end
            .catch Ljava/lang/Throwable; {:clear_start .. :clear_end} :clear_failed
            :close
            if-eqz v1, :cleared
            :close_start
            invoke-virtual {v1}, Landroid/database/sqlite/SQLiteDatabase;->endTransaction()V
            :close_end
            .catch Ljava/lang/Throwable; {:close_start .. :close_end} :close_failed
            :cleared
            const v0, 0x7f12018a
            const/4 v1, 0x0
            invoke-static {v0, v1}, Lwf0;->O0(II)V
            const/4 v0, 0x0
            return-object v0
            :clear_failed
            move-exception v2
            invoke-virtual {v2}, Ljava/lang/Throwable;->printStackTrace()V
            goto :close
            :close_failed
            move-exception v2
            invoke-virtual {v2}, Ljava/lang/Throwable;->printStackTrace()V
            goto :cleared
            :backup
            iget-object v0, p0, Lkb;->e:Ljava/lang/Object;
            check-cast v0, Lk00;
            :backup_start
            invoke-static {}, $APP_PACKAGE/MyApplication;->k()Z
            move-result v1
            if-nez v1, :ready
            const/4 v1, 0x1
            sput-boolean v1, $APP_PACKAGE/MyApplication;->R:Z
            sget-object v1, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;
            return-object v1
            :ready
            iget-boolean v1, p0, Lkb;->c:Z
            invoke-static {v0, v1}, Lmb;->a(Lk00;Z)V
            invoke-static {}, Lmb;->j()V
            :backup_end
            .catch Ljava/lang/Throwable; {:backup_start .. :backup_end} :backup_failed
            const/4 v0, 0x1
            invoke-static {v0}, Ljava/lang/Boolean;->valueOf(Z)Ljava/lang/Boolean;
            move-result-object v0
            return-object v0
            :backup_failed
            move-exception v1
            invoke-virtual {v1}, Ljava/lang/Throwable;->printStackTrace()V
            :discard_start
            invoke-virtual {v0}, Lk00;->e()Z
            :discard_end
            .catch Ljava/lang/Throwable; {:discard_start .. :discard_end} :discard_failed
            :failed_toast
            const v0, 0x7f1201dd
            const/4 v1, 0x0
            invoke-static {v0, v1}, Lwf0;->O0(II)V
            const/4 v0, 0x0
            return-object v0
            :discard_failed
            invoke-virtual {v1}, Ljava/lang/Throwable;->printStackTrace()V
            goto :failed_toast
        """,
    ),
    RestoredMethod(
        "Lc51;", DO_IN_BACKGROUND, 7,
        """
            :start
            invoke-static {}, Ljava/util/Calendar;->getInstance()Ljava/util/Calendar;
            move-result-object v0
            sget-object v1, Lem0;->M:Ljava/util/Calendar;
            const/4 v2, 0x5
            invoke-virtual {v0, v2}, Ljava/util/Calendar;->get(I)I
            move-result v3
            invoke-virtual {v1, v2}, Ljava/util/Calendar;->get(I)I
            move-result v4
            if-ne v3, v4, :changed
            const/4 v2, 0x2
            invoke-virtual {v0, v2}, Ljava/util/Calendar;->get(I)I
            move-result v3
            invoke-virtual {v1, v2}, Ljava/util/Calendar;->get(I)I
            move-result v4
            if-ne v3, v4, :changed
            const/4 v2, 0x1
            invoke-virtual {v0, v2}, Ljava/util/Calendar;->get(I)I
            move-result v3
            invoke-virtual {v1, v2}, Ljava/util/Calendar;->get(I)I
            move-result v4
            if-eq v3, v4, :same
            :changed
            const/4 v2, 0x1
            iput-boolean v2, p0, Lc51;->a:Z
            :same
            sput-object v0, Lem0;->M:Ljava/util/Calendar;
            :end
            .catch Ljava/lang/Throwable; {:start .. :end} :failed
            :done
            const/4 v0, 0x0
            return-object v0
            :failed
            move-exception v0
            invoke-virtual {v0}, Ljava/lang/Throwable;->printStackTrace()V
            goto :done
        """,
    ),
    RestoredMethod(
        "Lez0;", DO_IN_BACKGROUND, 4,
        """
            :start
            const/4 v0, 0x0
            sput-boolean v0, $APP_PACKAGE/MusicService;->h1:Z
            sget-boolean v0, $APP_PACKAGE/ReplayGainReadCalcService;->t:Z
            if-eqz v0, :collect
            const/4 v0, 0x1
            sput-boolean v0, $APP_PACKAGE/MusicService;->h1:Z
            goto :done
            :collect
            new-instance v0, Ljava/util/HashSet;
            const/4 v1, 0x2
            invoke-direct {v0, v1}, Ljava/util/HashSet;-><init>(I)V
            iput-object v0, p0, Lez0;->c:Ljava/util/HashSet;
            iget v1, p0, Lez0;->a:I
            invoke-static {v1, v0}, $APP_PACKAGE/MusicService;->A(ILjava/util/HashSet;)V
            :end
            .catch Ljava/lang/Throwable; {:start .. :end} :failed
            :done
            const/4 v0, 0x0
            return-object v0
            :failed
            move-exception v0
            invoke-virtual {v0}, Ljava/lang/Throwable;->printStackTrace()V
            goto :done
        """,
    ),
    RestoredMethod(
        "Ljm1;", DO_IN_BACKGROUND, 11,
        """
            :start
            sget-object v0, $APP_PACKAGE/MyApplication;->i:$APP_PACKAGE/MyApplication;
            invoke-virtual {v0}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;
            move-result-object v0
            invoke-static {v0}, Ldi0;->m(Landroid/content/Context;)Z
            move-result v1
            if-nez v1, :supported
            const v0, 0x7f12051f
            const/4 v1, 0x1
            invoke-static {v0, v1}, Lwf0;->O0(II)V
            goto :done
            :supported
            new-instance v1, Landroid/content/Intent;
            iget-object v2, p0, Ljm1;->a:Ljava/lang/String;
            invoke-direct {v1, v2}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V
            const-class v2, $APP_PACKAGE/MusicActivity;
            invoke-virtual {v1, v0, v2}, Landroid/content/Intent;->setClass(Landroid/content/Context;Ljava/lang/Class;)Landroid/content/Intent;
            const-string v3, "extshjp_"
            iget-object v4, p0, Ljm1;->b:Ljava/lang/String;
            invoke-virtual {v1, v3, v4}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;
            new-instance v4, Ljava/lang/StringBuilder;
            invoke-direct {v4}, Ljava/lang/StringBuilder;-><init>()V
            invoke-virtual {v1}, Landroid/content/Intent;->getAction()Ljava/lang/String;
            move-result-object v5
            invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
            invoke-virtual {v1, v3}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;
            move-result-object v5
            invoke-virtual {v4, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
            invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
            move-result-object v4
            new-instance v5, Lhm1;
            invoke-direct {v5}, Lhm1;-><init>()V
            iput-object v0, v5, Lhm1;->a:Landroid/content/Context;
            iput-object v4, v5, Lhm1;->b:Ljava/lang/String;
            const/4 v6, 0x1
            new-array v6, v6, [Landroid/content/Intent;
            const/4 v7, 0x0
            aput-object v1, v6, v7
            iput-object v6, v5, Lhm1;->c:[Landroid/content/Intent;
            new-instance v6, Landroid/content/ComponentName;
            invoke-direct {v6, v0, v2}, Landroid/content/ComponentName;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
            iput-object v6, v5, Lhm1;->d:Landroid/content/ComponentName;
            iget-object v6, p0, Ljm1;->c:Ljava/lang/String;
            iput-object v6, v5, Lhm1;->e:Ljava/lang/CharSequence;
            invoke-static {v6}, Landroid/text/TextUtils;->isEmpty(Ljava/lang/CharSequence;)Z
            move-result v6
            if-nez v6, :done
            invoke-virtual {v0}, Landroid/content/Context;->getResources()Landroid/content/res/Resources;
            move-result-object v6
            invoke-virtual {v0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;
            move-result-object v7
            iget v8, p0, Ljm1;->e:I
            invoke-static {v6, v7, v8}, Landroidx/core/graphics/drawable/IconCompat;->b(Landroid/content/res/Resources;Ljava/lang/String;I)Landroidx/core/graphics/drawable/IconCompat;
            move-result-object v6
            iput-object v6, v5, Lhm1;->f:Landroidx/core/graphics/drawable/IconCompat;
            invoke-static {v0, v5}, Ldi0;->s(Landroid/content/Context;Lhm1;)V
            :end
            .catch Ljava/lang/Throwable; {:start .. :end} :failed
            :done
            const/4 v0, 0x0
            return-object v0
            :failed
            move-exception v0
            invoke-virtual {v0}, Ljava/lang/Throwable;->printStackTrace()V
            goto :done
        """,
    ),
    RestoredMethod(
        "$APP_PACKAGE/fh;", ON_RECEIVE, 6,
        """
            :start
            new-instance v0, Landroid/content/Intent;
            invoke-direct {v0, p2}, Landroid/content/Intent;-><init>(Landroid/content/Intent;)V
            sget-object v1, $APP_PACKAGE/MyApplication;->i:$APP_PACKAGE/MyApplication;
            invoke-virtual {v1}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;
            move-result-object v1
            const-string v2, "tclass"
            invoke-virtual {p2, v2}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;
            move-result-object v2
            invoke-static {v2}, Ljava/lang/Class;->forName(Ljava/lang/String;)Ljava/lang/Class;
            move-result-object v2
            invoke-virtual {v0, v1, v2}, Landroid/content/Intent;->setClass(Landroid/content/Context;Ljava/lang/Class;)Landroid/content/Intent;
            const-string v1, "E_IA"
            const/4 v2, 0x0
            invoke-virtual {v0, v1, v2}, Landroid/content/Intent;->getBooleanExtra(Ljava/lang/String;Z)Z
            move-result v1
            if-nez v1, :activity
            invoke-static {p1, v0}, $APP_PACKAGE/fh;->a(Landroid/content/Context;Landroid/content/Intent;)V
            goto :end
            :activity
            const v1, 0x50800000
            invoke-virtual {v0, v1}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;
            invoke-virtual {p1, v0}, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V
            :end
            .catch Ljava/lang/Throwable; {:start .. :end} :failed
            :done
            return-void
            :failed
            move-exception v0
            invoke-virtual {v0}, Ljava/lang/Throwable;->printStackTrace()V
            goto :done
        """,
    ),
    RestoredMethod(
        "$APP_PACKAGE/RemoteMediaControlReceiver;", ON_RECEIVE, 3,
        """
            invoke-static {p1, p2}, $APP_PACKAGE/RemoteMediaControlReceiver;->a(Landroid/content/Context;Landroid/content/Intent;)V
            return-void
        """,
    ),
    RestoredMethod(
        "$APP_PACKAGE/MediaMountedReceiver;", ON_RECEIVE, 5,
        """
            invoke-virtual {p0}, Landroid/content/BroadcastReceiver;->isInitialStickyBroadcast()Z
            move-result v0
            if-nez v0, :done
            new-instance v0, Ljava/lang/StringBuilder;
            const-string v1, "MMR OR "
            invoke-direct {v0, v1}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
            invoke-virtual {v0, p2}, Ljava/lang/StringBuilder;->append(Ljava/lang/Object;)Ljava/lang/StringBuilder;
            invoke-virtual {v0}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
            move-result-object v0
            invoke-static {v0}, Lv82;->y(Ljava/lang/String;)V
            invoke-static {}, $APP_PACKAGE/MediaMountedReceiver;->a()V
            :done
            return-void
        """,
    ),
)

private val UNRESTORED_STUB_TYPES = setOf(
    "Lca2;",
    "Lci2;",
    "Ld9;",
    "Lov;",
    "Landroidx/mediarouter/media/MediaTransferReceiver;",
    "Landroidx/work/impl/background/systemalarm/RescheduleReceiver;",
    "Landroidx/work/impl/diagnostics/DiagnosticsReceiver;",
    "Landroidx/work/impl/utils/ForceStopRunnable\$BroadcastReceiver;",
    "Lcom/google/android/datatransport/runtime/scheduling/jobscheduling/AlarmManagerSchedulerBroadcastReceiver;",
)

private fun Method.callsVmRunner() = implementation?.instructions?.any { instruction ->
    ((instruction as? ReferenceInstruction)?.reference as? MethodReference)?.definingClass == VM_RUNNER
} == true

val restoreVirtualizedMethodsPatch = bytecodePatch {
    compatibleWith(AppCompatibilities.MUSICOLET)

    dependsOn(removePairipVirtualizationPatch)

    execute {
        RESTORED_METHODS.forEach { restored ->
            mutableClassDefBy(restored.type).apply {
                check(methods.single { it.name == restored.name }.callsVmRunner()) {
                    "${restored.type}->${restored.name} is not a PairIP stub in this build"
                }
                replaceMethod(restored.name, restored.registers, restored.smali)
            }
        }
        mutableClassDefBy("Lhm1;").addDefaultConstructor()

        val leftover = mutableSetOf<String>()
        classDefForEach { classDef ->
            if (!classDef.type.startsWith("Lcom/pairip/") && classDef.methods.any { it.callsVmRunner() }) {
                leftover += classDef.type
            }
        }
        check(leftover == UNRESTORED_STUB_TYPES) {
            "PairIP stubs changed: unexpected ${leftover - UNRESTORED_STUB_TYPES}, missing ${UNRESTORED_STUB_TYPES - leftover}"
        }
    }
}
