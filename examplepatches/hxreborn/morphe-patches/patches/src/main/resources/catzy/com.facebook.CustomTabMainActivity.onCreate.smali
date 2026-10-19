    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    invoke-virtual {v0}, Landroid/content/Intent;->getAction()Ljava/lang/String;
    move-result-object v0
    const-string v1, "CustomTabActivity.action_customTabRedirect"
    invoke-static {v1, v0}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v0
    const/4 v2, 0x0
    if-eqz v0, :cond_1b
    invoke-virtual {p0, v2}, Landroid/app/Activity;->setResult(I)V
    invoke-virtual {p0}, Landroid/app/Activity;->finish()V
    return-void
    :cond_1b
    if-nez p1, :cond_14a
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v0, "CustomTabMainActivity.extra_action"
    invoke-virtual {p1, v0}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;
    move-result-object p1
    if-nez p1, :cond_2b
    goto/16 :goto_14a
    :cond_2b
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    const-string v3, "CustomTabMainActivity.extra_params"
    invoke-virtual {v0, v3}, Landroid/content/Intent;->getBundleExtra(Ljava/lang/String;)Landroid/os/Bundle;
    move-result-object v0
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v3
    const-string v4, "CustomTabMainActivity.extra_chromePackage"
    invoke-virtual {v3, v4}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v3
    sget-object v4, Lcom/facebook/login/e0;->Companion:Lcom/facebook/login/d0;
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v5
    const-string v6, "CustomTabMainActivity.extra_targetApp"
    invoke-virtual {v5, v6}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v5
    invoke-virtual {v4}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    invoke-static {}, Lcom/facebook/login/e0;->values()[Lcom/facebook/login/e0;
    move-result-object v4
    array-length v6, v4
    move v7, v2
    :goto_54
    if-ge v7, v6, :cond_66
    aget-object v8, v4, v7
    invoke-virtual {v8}, Lcom/facebook/login/e0;->toString()Ljava/lang/String;
    move-result-object v9
    invoke-static {v9, v5}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v9
    if-eqz v9, :cond_63
    goto :goto_68
    :cond_63
    add-int/lit8 v7, v7, 0x1
    goto :goto_54
    :cond_66
    sget-object v8, Lcom/facebook/login/e0;->FACEBOOK:Lcom/facebook/login/e0;
    :goto_68
    sget-object v4, Ly7/m;->a:[I
    invoke-virtual {v8}, Ljava/lang/Enum;->ordinal()I
    move-result v5
    aget v4, v4, v5
    const/4 v5, 0x1
    if-ne v4, v5, :cond_c3
    new-instance v4, Lx8/b0;
    invoke-direct {v4, p1, v0}, Lv6/k;-><init>(Ljava/lang/String;Landroid/os/Bundle;)V
    if-nez v0, :cond_7f
    new-instance v0, Landroid/os/Bundle;
    invoke-direct {v0}, Landroid/os/Bundle;-><init>()V
    :cond_7f
    const-string v6, "oauth"
    invoke-static {p1, v6}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v6
    if-eqz v6, :cond_92
    invoke-static {}, Lz7/c;->l()Ljava/lang/String;
    move-result-object p1
    const-string v6, "oauth/authorize"
    invoke-static {p1, v6, v0}, Lx8/n0;->a(Ljava/lang/String;Ljava/lang/String;Landroid/os/Bundle;)Landroid/net/Uri;
    move-result-object p1
    goto :goto_b2
    :cond_92
    invoke-static {}, Lz7/c;->l()Ljava/lang/String;
    move-result-object v6
    new-instance v7, Ljava/lang/StringBuilder;
    invoke-direct {v7}, Ljava/lang/StringBuilder;-><init>()V
    invoke-static {}, Ly7/a0;->d()Ljava/lang/String;
    move-result-object v8
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    const-string v8, "/dialog/"
    invoke-virtual {v7, v8}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v7, p1}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v7}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object p1
    invoke-static {v6, p1, v0}, Lx8/n0;->a(Ljava/lang/String;Ljava/lang/String;Landroid/os/Bundle;)Landroid/net/Uri;
    move-result-object p1
    :goto_b2
    sget-object v0, Lc9/a;->a:Ljava/util/Set;
    invoke-interface {v0, v4}, Ljava/util/Set;->contains(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :cond_bb
    goto :goto_c8
    :cond_bb
    :try_start_bb
    iput-object p1, v4, Lv6/k;->a:Ljava/lang/Object;
    :try_end_bd
    .catchall {:try_start_bb .. :try_end_bd} :catchall_be
    goto :goto_c8
    :catchall_be
    move-exception p1
    invoke-static {v4, p1}, Lc9/a;->a(Ljava/lang/Object;Ljava/lang/Throwable;)V
    goto :goto_c8
    :cond_c3
    new-instance v4, Lv6/k;
    invoke-direct {v4, p1, v0}, Lv6/k;-><init>(Ljava/lang/String;Landroid/os/Bundle;)V
    :goto_c8
    sget-object p1, Lc9/a;->a:Ljava/util/Set;
    invoke-interface {p1, v4}, Ljava/util/Set;->contains(Ljava/lang/Object;)Z
    move-result p1
    if-eqz p1, :cond_d2
    :catch_d0
    :goto_d0
    move p1, v2
    goto :goto_121
    :cond_d2
    :try_start_d2
    sget-object p1, Lcom/facebook/login/d;->c:Ljava/util/concurrent/locks/ReentrantLock;
    invoke-virtual {p1}, Ljava/util/concurrent/locks/ReentrantLock;->lock()V
    sget-object v0, Lcom/facebook/login/d;->b:Li/j0;
    const/4 v6, 0x0
    sput-object v6, Lcom/facebook/login/d;->b:Li/j0;
    invoke-virtual {p1}, Ljava/util/concurrent/locks/ReentrantLock;->unlock()V
    new-instance p1, Landroid/content/Intent;
    const-string v7, "android.intent.action.VIEW"
    invoke-direct {p1, v7}, Landroid/content/Intent;-><init>(Ljava/lang/String;)V
    if-eqz v0, :cond_f3
    iget-object v7, v0, Li/j0;->d:Ljava/lang/Object;
    check-cast v7, Landroid/content/ComponentName;
    invoke-virtual {v7}, Landroid/content/ComponentName;->getPackageName()Ljava/lang/String;
    move-result-object v7
    invoke-virtual {p1, v7}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;
    :cond_f3
    new-instance v7, Landroid/os/Bundle;
    invoke-direct {v7}, Landroid/os/Bundle;-><init>()V
    if-nez v0, :cond_fc
    move-object v0, v6
    goto :goto_100
    :cond_fc
    iget-object v0, v0, Li/j0;->c:Ljava/lang/Object;
    check-cast v0, Lv/b;
    :goto_100
    const-string v8, "android.support.customtabs.extra.SESSION"
    invoke-virtual {v7, v8, v0}, Landroid/os/Bundle;->putBinder(Ljava/lang/String;Landroid/os/IBinder;)V
    invoke-virtual {p1, v7}, Landroid/content/Intent;->putExtras(Landroid/os/Bundle;)Landroid/content/Intent;
    const-string v0, "android.support.customtabs.extra.EXTRA_ENABLE_INSTANT_APPS"
    invoke-virtual {p1, v0, v5}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Z)Landroid/content/Intent;
    invoke-virtual {p1, v3}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;
    :try_end_110
    .catchall {:try_start_d2 .. :try_end_110} :catchall_11c
    :try_start_110
    iget-object v0, v4, Lv6/k;->a:Ljava/lang/Object;
    check-cast v0, Landroid/net/Uri;
    invoke-virtual {p1, v0}, Landroid/content/Intent;->setData(Landroid/net/Uri;)Landroid/content/Intent;
    invoke-virtual {p0, p1, v6}, Landroid/content/Context;->startActivity(Landroid/content/Intent;Landroid/os/Bundle;)V
    :try_end_11a
    .catch Landroid/content/ActivityNotFoundException; {:try_start_110 .. :try_end_11a} :catch_d0
    .catchall {:try_start_110 .. :try_end_11a} :catchall_11c
    move p1, v5
    goto :goto_121
    :catchall_11c
    move-exception p1
    invoke-static {v4, p1}, Lc9/a;->a(Ljava/lang/Object;Ljava/lang/Throwable;)V
    goto :goto_d0
    :goto_121
    iput-boolean v2, p0, Lcom/facebook/CustomTabMainActivity;->a:Z
    if-nez p1, :cond_136
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v0, "CustomTabMainActivity.no_activity_exception"
    invoke-virtual {p1, v0, v5}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Z)Landroid/content/Intent;
    move-result-object p1
    invoke-virtual {p0, v2, p1}, Landroid/app/Activity;->setResult(ILandroid/content/Intent;)V
    invoke-virtual {p0}, Landroid/app/Activity;->finish()V
    return-void
    :cond_136
    new-instance p1, Ldb/b5;
    const/4 v0, 0x5
    invoke-direct {p1, p0, v0}, Ldb/b5;-><init>(Ljava/lang/Object;I)V
    iput-object p1, p0, Lcom/facebook/CustomTabMainActivity;->b:Ldb/b5;
    invoke-static {p0}, Lf2/c;->a(Landroid/content/Context;)Lf2/c;
    move-result-object v0
    new-instance v2, Landroid/content/IntentFilter;
    invoke-direct {v2, v1}, Landroid/content/IntentFilter;-><init>(Ljava/lang/String;)V
    invoke-virtual {v0, p1, v2}, Lf2/c;->b(Landroid/content/BroadcastReceiver;Landroid/content/IntentFilter;)V
    :cond_14a
    :goto_14a
    return-void
