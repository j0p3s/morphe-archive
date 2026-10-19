    invoke-super {p0, p1}, Landroidx/fragment/app/i0;->onCreate(Landroid/os/Bundle;)V
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    invoke-virtual {v0}, Landroid/content/Intent;->getAction()Ljava/lang/String;
    move-result-object v1
    const/16 v2, 0x30d4
    const-string v3, "AuthSignInClient"
    if-nez v1, :cond_1a
    const-string p1, "Null action"
    invoke-static {v3, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I
    invoke-virtual {p0, v2}, Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;->b0(I)V
    return-void
    :cond_1a
    const-string v4, "com.google.android.gms.auth.NO_IMPL"
    invoke-virtual {v1, v4}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v4
    if-eqz v4, :cond_2b
    const-string p1, "Action not implemented"
    invoke-static {v3, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I
    invoke-virtual {p0, v2}, Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;->b0(I)V
    return-void
    :cond_2b
    const-string v2, "com.google.android.gms.auth.GOOGLE_SIGN_IN"
    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v4
    if-nez v4, :cond_51
    const-string v4, "com.google.android.gms.auth.APPAUTH_SIGN_IN"
    invoke-virtual {v1, v4}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v4
    if-eqz v4, :cond_3c
    goto :goto_51
    :cond_3c
    invoke-virtual {v0}, Landroid/content/Intent;->getAction()Ljava/lang/String;
    move-result-object p1
    invoke-static {p1}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
    move-result-object p1
    const-string v0, "Unknown action: "
    invoke-virtual {v0, p1}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
    move-result-object p1
    invoke-static {v3, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I
    invoke-virtual {p0}, Landroid/app/Activity;->finish()V
    return-void
    :cond_51
    :goto_51
    const-string v4, "config"
    invoke-virtual {v0, v4}, Landroid/content/Intent;->getBundleExtra(Ljava/lang/String;)Landroid/os/Bundle;
    move-result-object v0
    const/4 v5, 0x0
    if-nez v0, :cond_66
    const-string p1, "Activity started with no configuration."
    invoke-static {v3, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I
    invoke-virtual {p0, v5}, Landroid/app/Activity;->setResult(I)V
    invoke-virtual {p0}, Landroid/app/Activity;->finish()V
    return-void
    :cond_66
    invoke-virtual {v0, v4}, Landroid/os/Bundle;->getParcelable(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v0
    check-cast v0, Lcom/google/android/gms/auth/api/signin/internal/SignInConfiguration;
    if-nez v0, :cond_7a
    const-string p1, "Activity started with invalid configuration."
    invoke-static {v3, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I
    invoke-virtual {p0, v5}, Landroid/app/Activity;->setResult(I)V
    invoke-virtual {p0}, Landroid/app/Activity;->finish()V
    return-void
    :cond_7a
    iput-object v0, p0, Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;->b:Lcom/google/android/gms/auth/api/signin/internal/SignInConfiguration;
    if-nez p1, :cond_bf
    sget-boolean p1, Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;->f:Z
    if-eqz p1, :cond_8b
    invoke-virtual {p0, v5}, Landroid/app/Activity;->setResult(I)V
    const/16 p1, 0x30d6
    invoke-virtual {p0, p1}, Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;->b0(I)V
    return-void
    :cond_8b
    const/4 p1, 0x1
    sput-boolean p1, Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;->f:Z
    new-instance v0, Landroid/content/Intent;
    invoke-direct {v0, v1}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V
    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :cond_9f
    const-string v1, "com.google.android.gms"
    invoke-virtual {v0, v1}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;
    goto :goto_a6
    :cond_9f
    invoke-virtual {p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v0, v1}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;
    :goto_a6
    iget-object v1, p0, Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;->b:Lcom/google/android/gms/auth/api/signin/internal/SignInConfiguration;
    invoke-virtual {v0, v4, v1}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Landroid/os/Parcelable;)Landroid/content/Intent;
    const v1, 0xa002
    :try_start_ae
    invoke-virtual {p0, v0, v1}, Landroidx/activity/t;->startActivityForResult(Landroid/content/Intent;I)V
    :try_end_b1
    .catch Landroid/content/ActivityNotFoundException; {:try_start_ae .. :try_end_b1} :catch_b2
    return-void
    :catch_b2
    iput-boolean p1, p0, Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;->a:Z
    const-string p1, "Could not launch sign in Intent. Google Play Service is probably being updated..."
    invoke-static {v3, p1}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I
    const/16 p1, 0x11
    invoke-virtual {p0, p1}, Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;->b0(I)V
    goto :goto_ec
    :cond_bf
    const-string v0, "signingInGoogleApiClients"
    invoke-virtual {p1, v0}, Landroid/os/BaseBundle;->getBoolean(Ljava/lang/String;)Z
    move-result v0
    iput-boolean v0, p0, Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;->c:Z
    if-eqz v0, :cond_ec
    const-string v0, "signInResultCode"
    invoke-virtual {p1, v0}, Landroid/os/BaseBundle;->getInt(Ljava/lang/String;)I
    move-result v0
    iput v0, p0, Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;->d:I
    const-string v0, "signInResultData"
    invoke-virtual {p1, v0}, Landroid/os/Bundle;->getParcelable(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object p1
    check-cast p1, Landroid/content/Intent;
    if-nez p1, :cond_e7
    const-string p1, "Sign in result data cannot be null"
    invoke-static {v3, p1}, Landroid/util/Log;->e(Ljava/lang/String;Ljava/lang/String;)I
    invoke-virtual {p0, v5}, Landroid/app/Activity;->setResult(I)V
    invoke-virtual {p0}, Landroid/app/Activity;->finish()V
    return-void
    :cond_e7
    iput-object p1, p0, Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;->e:Landroid/content/Intent;
    invoke-virtual {p0}, Lcom/google/android/gms/auth/api/signin/internal/SignInHubActivity;->a0()V
    :cond_ec
    :goto_ec
    return-void
