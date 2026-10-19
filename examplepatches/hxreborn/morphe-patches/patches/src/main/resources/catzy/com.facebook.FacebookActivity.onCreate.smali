    invoke-super {p0, p1}, Landroidx/fragment/app/i0;->onCreate(Landroid/os/Bundle;)V
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    sget-object v0, Ly7/a0;->q:Ljava/util/concurrent/atomic/AtomicBoolean;
    invoke-virtual {v0}, Ljava/util/concurrent/atomic/AtomicBoolean;->get()Z
    move-result v0
    if-nez v0, :cond_1e
    invoke-virtual {p0}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;
    move-result-object v0
    const-class v1, Ly7/a0;
    monitor-enter v1
    :try_start_16
    invoke-static {v0}, Ly7/a0;->i(Landroid/content/Context;)V
    :try_end_19
    .catchall {:try_start_16 .. :try_end_19} :catchall_1b
    monitor-exit v1
    goto :goto_1e
    :catchall_1b
    move-exception p1
    :try_start_1c
    monitor-exit v1
    :try_end_1d
    .catchall {:try_start_1c .. :try_end_1d} :catchall_1b
    throw p1
    :cond_1e
    :goto_1e
    const v0, 0x7f0d00cf
    invoke-virtual {p0, v0}, Landroidx/activity/t;->setContentView(I)V
    const-string v0, "PassThrough"
    invoke-virtual {p1}, Landroid/content/Intent;->getAction()Ljava/lang/String;
    move-result-object p1
    invoke-static {v0, p1}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result p1
    const/4 v0, 0x0
    if-eqz p1, :cond_90
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    invoke-static {p1}, Lx8/h0;->i(Landroid/content/Intent;)Landroid/os/Bundle;
    move-result-object p1
    sget-object v1, Lc9/a;->a:Ljava/util/Set;
    const-class v2, Lx8/h0;
    invoke-interface {v1, v2}, Ljava/util/Set;->contains(Ljava/lang/Object;)Z
    move-result v1
    const/4 v3, 0x0
    if-eqz v1, :cond_45
    :goto_43
    move-object p1, v3
    goto :goto_81
    :cond_45
    if-nez p1, :cond_48
    goto :goto_43
    :cond_48
    :try_start_48
    const-string v1, "error_type"
    invoke-virtual {p1, v1}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v1
    if-nez v1, :cond_59
    const-string v1, "com.facebook.platform.status.ERROR_TYPE"
    invoke-virtual {p1, v1}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v1
    goto :goto_59
    :catchall_57
    move-exception p1
    goto :goto_7d
    :cond_59
    :goto_59
    const-string v4, "error_description"
    invoke-virtual {p1, v4}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v4
    if-nez v4, :cond_67
    const-string v4, "com.facebook.platform.status.ERROR_DESCRIPTION"
    invoke-virtual {p1, v4}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v4
    :cond_67
    if-eqz v1, :cond_77
    const-string p1, "UserCanceled"
    invoke-virtual {v1, p1}, Ljava/lang/String;->equalsIgnoreCase(Ljava/lang/String;)Z
    move-result p1
    if-eqz p1, :cond_77
    new-instance p1, Ly7/w;
    invoke-direct {p1, v4}, Ly7/w;-><init>(Ljava/lang/String;)V
    goto :goto_81
    :cond_77
    new-instance p1, Ly7/t;
    invoke-direct {p1, v4}, Ly7/t;-><init>(Ljava/lang/String;)V
    :try_end_7c
    .catchall {:try_start_48 .. :try_end_7c} :catchall_57
    goto :goto_81
    :goto_7d
    invoke-static {v2, p1}, Lc9/a;->a(Ljava/lang/Object;Ljava/lang/Throwable;)V
    goto :goto_43
    :goto_81
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v1
    invoke-static {v1, v3, p1}, Lx8/h0;->e(Landroid/content/Intent;Landroid/os/Bundle;Ly7/t;)Landroid/content/Intent;
    move-result-object p1
    invoke-virtual {p0, v0, p1}, Landroid/app/Activity;->setResult(ILandroid/content/Intent;)V
    invoke-virtual {p0}, Landroid/app/Activity;->finish()V
    return-void
    :cond_90
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    invoke-virtual {p0}, Landroidx/fragment/app/i0;->getSupportFragmentManager()Landroidx/fragment/app/d1;
    move-result-object v1
    const-string v2, "SingleFragment"
    invoke-virtual {v1, v2}, Landroidx/fragment/app/d1;->D(Ljava/lang/String;)Landroidx/fragment/app/x;
    move-result-object v3
    if-nez v3, :cond_d0
    const-string v3, "FacebookDialogFragment"
    invoke-virtual {p1}, Landroid/content/Intent;->getAction()Ljava/lang/String;
    move-result-object p1
    invoke-static {v3, p1}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result p1
    const/4 v3, 0x1
    if-eqz p1, :cond_ba
    new-instance p1, Lcom/facebook/internal/FacebookDialogFragment;
    invoke-direct {p1}, Lcom/facebook/internal/FacebookDialogFragment;-><init>()V
    invoke-virtual {p1, v3}, Landroidx/fragment/app/x;->setRetainInstance(Z)V
    invoke-virtual {p1, v1, v2}, Landroidx/fragment/app/DialogFragment;->p(Landroidx/fragment/app/d1;Ljava/lang/String;)V
    :goto_b8
    move-object v3, p1
    goto :goto_d0
    :cond_ba
    new-instance p1, Lcom/facebook/login/LoginFragment;
    invoke-direct {p1}, Lcom/facebook/login/LoginFragment;-><init>()V
    invoke-virtual {p1, v3}, Landroidx/fragment/app/x;->setRetainInstance(Z)V
    new-instance v4, Landroidx/fragment/app/a;
    invoke-direct {v4, v1}, Landroidx/fragment/app/a;-><init>(Landroidx/fragment/app/d1;)V
    const v1, 0x7f0a019d
    invoke-virtual {v4, v1, p1, v2, v3}, Landroidx/fragment/app/a;->g(ILandroidx/fragment/app/x;Ljava/lang/String;I)V
    invoke-virtual {v4, v0}, Landroidx/fragment/app/a;->d(Z)I
    goto :goto_b8
    :cond_d0
    :goto_d0
    iput-object v3, p0, Lcom/facebook/FacebookActivity;->a:Landroidx/fragment/app/x;
    return-void
