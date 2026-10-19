    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V
    const/4 v1, 0x0
    if-nez p1, :cond_19
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    if-nez v0, :cond_e
    move v0, v1
    goto :goto_20
    :cond_e
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    const-string v2, "IN_APP_MESSAGE_INTENT"
    invoke-virtual {v0, v2}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z
    move-result v0
    goto :goto_20
    :cond_19
    const/4 v0, 0x0
    const-string v0, "in_app_message_result_receiver"
    invoke-virtual {p1, v0}, Landroid/os/BaseBundle;->containsKey(Ljava/lang/String;)Z
    move-result v0
    :goto_20
    const/4 v2, 0x0
    const/4 v3, 0x1
    if-nez v0, :cond_d4
    :try_start_24
    invoke-virtual {p0}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;
    move-result-object v0
    invoke-virtual {p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;
    move-result-object v4
    invoke-virtual {v0, v4, v1}, Landroid/content/pm/PackageManager;->getPackageInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;
    move-result-object v0
    iget v0, v0, Landroid/content/pm/PackageInfo;->versionCode:I
    :try_end_32
    .catch Landroid/content/pm/PackageManager$NameNotFoundException; {:try_start_24 .. :try_end_32} :catch_33
    goto :goto_3c
    :catch_33
    move-exception v0
    const-string v4, "ProxyBillingActivity"
    const-string v5, "Failed to get package info for current package."
    invoke-static {v4, v5, v0}, Lcom/google/android/gms/internal/play_billing/s;->i(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
    const/4 v0, -0x1
    :goto_3c
    iget-object v4, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->h:Landroidx/work/impl/model/c;
    if-nez v4, :cond_6a
    invoke-virtual {p0}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;
    move-result-object v4
    invoke-static {}, Lcom/google/android/gms/internal/play_billing/r4;->B()Lcom/google/android/gms/internal/play_billing/q4;
    move-result-object v5
    invoke-virtual {p0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;
    move-result-object v6
    invoke-virtual {v5, v6}, Lcom/google/android/gms/internal/play_billing/q4;->l(Ljava/lang/String;)V
    invoke-virtual {v5}, Lcom/google/android/gms/internal/play_billing/q4;->m()V
    invoke-virtual {v5, v0}, Lcom/google/android/gms/internal/play_billing/q4;->h(I)V
    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I
    invoke-virtual {v5, v0}, Lcom/google/android/gms/internal/play_billing/q4;->g(I)V
    invoke-virtual {v5}, Lcom/google/android/gms/internal/play_billing/q4;->k()V
    invoke-virtual {v5}, Lcom/google/android/gms/internal/play_billing/m2;->b()Lcom/google/android/gms/internal/play_billing/n2;
    move-result-object v0
    check-cast v0, Lcom/google/android/gms/internal/play_billing/r4;
    new-instance v5, Landroidx/work/impl/model/c;
    invoke-direct {v5, v4, v0}, Landroidx/work/impl/model/c;-><init>(Landroid/content/Context;Lcom/google/android/gms/internal/play_billing/r4;)V
    iput-object v5, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->h:Landroidx/work/impl/model/c;
    :cond_6a
    monitor-enter p0
    const/4 v4, 0x2
    :try_start_6c
    new-instance v0, Lg6/k0;
    iget-object v5, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->h:Landroidx/work/impl/model/c;
    invoke-direct {v0, v5}, Lg6/k0;-><init>(Landroidx/work/impl/model/c;)V
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->g:Lg6/k0;
    new-instance v0, Landroid/content/IntentFilter;
    const-string v5, "com.android.vending.billing.IN_APP_BILLING_RESULT_UPDATE_ACTION"
    invoke-direct {v0, v5}, Landroid/content/IntentFilter;-><init>(Ljava/lang/String;)V
    const-string v5, "com.android.vending.billing.PLAY_BILLING_ACTIVITY_CREATED_ACTION"
    invoke-virtual {v0, v5}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V
    iget-object v5, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->g:Lg6/k0;
    const-string v6, "com.google.android.finsky.permission.PLAY_BILLING_LIBRARY_BROADCAST"
    invoke-static {p0, v5, v0, v6, v4}, Lm0/a;->e(Landroid/content/Context;Landroid/content/BroadcastReceiver;Landroid/content/IntentFilter;Ljava/lang/String;I)Landroid/content/Intent;
    :try_end_88
    .catch Ljava/lang/RuntimeException; {:try_start_6c .. :try_end_88} :catch_8f
    .catch Ljava/lang/NoSuchMethodError; {:try_start_6c .. :try_end_88} :catch_8d
    .catchall {:try_start_6c .. :try_end_88} :catchall_8a
    monitor-exit p0
    goto :goto_d4
    :catchall_8a
    move-exception v0
    move-object p1, v0
    goto :goto_d2
    :catch_8d
    move-exception v0
    goto :goto_90
    :catch_8f
    move-exception v0
    :goto_90
    :try_start_90
    iput-object v2, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->g:Lg6/k0;
    instance-of v5, v0, Ljava/lang/NoSuchMethodError;
    if-eqz v5, :cond_b0
    iget-object v5, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->h:Landroidx/work/impl/model/c;
    invoke-static {}, Lcom/google/android/gms/internal/play_billing/c5;->r()Lcom/google/android/gms/internal/play_billing/b5;
    move-result-object v6
    invoke-virtual {v6}, Lcom/google/android/gms/internal/play_billing/m2;->d()V
    iget-object v7, v6, Lcom/google/android/gms/internal/play_billing/m2;->b:Lcom/google/android/gms/internal/play_billing/n2;
    check-cast v7, Lcom/google/android/gms/internal/play_billing/c5;
    invoke-static {v7, v4}, Lcom/google/android/gms/internal/play_billing/c5;->s(Lcom/google/android/gms/internal/play_billing/c5;I)V
    invoke-virtual {v6}, Lcom/google/android/gms/internal/play_billing/m2;->b()Lcom/google/android/gms/internal/play_billing/n2;
    move-result-object v4
    check-cast v4, Lcom/google/android/gms/internal/play_billing/c5;
    invoke-virtual {v5, v4}, Landroidx/work/impl/model/c;->R0(Lcom/google/android/gms/internal/play_billing/c5;)V
    goto :goto_c9
    :cond_b0
    iget-object v4, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->h:Landroidx/work/impl/model/c;
    invoke-static {}, Lcom/google/android/gms/internal/play_billing/c5;->r()Lcom/google/android/gms/internal/play_billing/b5;
    move-result-object v5
    invoke-virtual {v5}, Lcom/google/android/gms/internal/play_billing/m2;->d()V
    iget-object v6, v5, Lcom/google/android/gms/internal/play_billing/m2;->b:Lcom/google/android/gms/internal/play_billing/n2;
    check-cast v6, Lcom/google/android/gms/internal/play_billing/c5;
    invoke-static {v6, v3}, Lcom/google/android/gms/internal/play_billing/c5;->s(Lcom/google/android/gms/internal/play_billing/c5;I)V
    invoke-virtual {v5}, Lcom/google/android/gms/internal/play_billing/m2;->b()Lcom/google/android/gms/internal/play_billing/n2;
    move-result-object v5
    check-cast v5, Lcom/google/android/gms/internal/play_billing/c5;
    invoke-virtual {v4, v5}, Landroidx/work/impl/model/c;->R0(Lcom/google/android/gms/internal/play_billing/c5;)V
    :goto_c9
    const-string v4, "ProxyBillingActivity"
    const-string v5, "Failed to register receiver."
    invoke-static {v4, v5, v0}, Lcom/google/android/gms/internal/play_billing/s;->i(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
    :try_end_d0
    .catchall {:try_start_90 .. :try_end_d0} :catchall_8a
    monitor-exit p0
    goto :goto_d4
    :goto_d2
    :try_start_d2
    monitor-exit p0
    :try_end_d3
    .catchall {:try_start_d2 .. :try_end_d3} :catchall_8a
    throw p1
    :cond_d4
    :goto_d4
    const/16 v0, 0x64
    if-nez p1, :cond_1e7
    const-string p1, "ProxyBillingActivity"
    const-string v4, "Launching Play Store billing flow"
    invoke-static {p1, v4}, Lcom/google/android/gms/internal/play_billing/s;->g(Ljava/lang/String;Ljava/lang/String;)V
    iput v0, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->d:I
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v0, "BUY_INTENT"
    invoke-virtual {p1, v0}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z
    move-result p1
    if-eqz p1, :cond_118
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v0, "BUY_INTENT"
    invoke-virtual {p1, v0}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object p1
    check-cast p1, Landroid/app/PendingIntent;
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    const-string v4, "IS_FLOW_FROM_FIRST_PARTY_CLIENT"
    invoke-virtual {v0, v4}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :cond_145
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    const-string v4, "IS_FLOW_FROM_FIRST_PARTY_CLIENT"
    invoke-virtual {v0, v4, v1}, Landroid/content/Intent;->getBooleanExtra(Ljava/lang/String;Z)Z
    move-result v0
    if-eqz v0, :cond_145
    iput-boolean v3, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->c:Z
    const/16 v0, 0x6e
    iput v0, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->d:I
    goto :goto_145
    :cond_118
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v0, "IN_APP_MESSAGE_INTENT"
    invoke-virtual {p1, v0}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z
    move-result p1
    if-eqz p1, :cond_144
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v0, "IN_APP_MESSAGE_INTENT"
    invoke-virtual {p1, v0}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object p1
    check-cast p1, Landroid/app/PendingIntent;
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    const/4 v4, 0x0
    const-string v4, "in_app_message_result_receiver"
    invoke-virtual {v0, v4}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v0
    check-cast v0, Landroid/os/ResultReceiver;
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->a:Landroid/os/ResultReceiver;
    const/16 v0, 0x65
    iput v0, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->d:I
    goto :goto_145
    :cond_144
    move-object p1, v2
    :cond_145
    :goto_145
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    const-string v4, "billingClientTransactionId"
    invoke-virtual {v0, v4}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :cond_15f
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    const-string v4, "billingClientTransactionId"
    const-wide/16 v5, 0x0
    invoke-virtual {v0, v4, v5, v6}, Landroid/content/Intent;->getLongExtra(Ljava/lang/String;J)J
    move-result-wide v4
    iput-wide v4, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->e:J
    :cond_15f
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    const-string v4, "wasServiceAutoReconnected"
    invoke-virtual {v0, v4}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :cond_177
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    const-string v4, "wasServiceAutoReconnected"
    invoke-virtual {v0, v4, v1}, Landroid/content/Intent;->getBooleanExtra(Ljava/lang/String;Z)Z
    move-result v0
    iput-boolean v0, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->f:Z
    :cond_177
    :try_start_177
    iput-boolean v3, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->b:Z
    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I
    :try_end_17b
    .catch Landroid/content/IntentSender$SendIntentException; {:try_start_177 .. :try_end_17b} :catch_1ba
    const/16 v4, 0x24
    if-lt v0, v4, :cond_191
    :try_start_17f
    invoke-static {}, Landroid/app/ActivityOptions;->makeBasic()Landroid/app/ActivityOptions;
    move-result-object v0
    invoke-static {v0}, Lcom/nieruo/healthapp/ui/activity/s2;->h(Landroid/app/ActivityOptions;)Landroid/app/ActivityOptions;
    move-result-object v0
    invoke-virtual {v0}, Landroid/app/ActivityOptions;->toBundle()Landroid/os/Bundle;
    move-result-object v0
    :goto_18b
    move-object v11, v0
    goto :goto_1a3
    :catch_18d
    move-exception v0
    move-object p1, v0
    move-object v4, p0
    goto :goto_1bd
    :cond_191
    const/16 v4, 0x22
    if-lt v0, v4, :cond_1a2
    invoke-static {}, Landroid/app/ActivityOptions;->makeBasic()Landroid/app/ActivityOptions;
    move-result-object v0
    invoke-static {v0}, Lcom/nieruo/healthapp/ui/activity/s2;->B(Landroid/app/ActivityOptions;)Landroid/app/ActivityOptions;
    move-result-object v0
    invoke-virtual {v0}, Landroid/app/ActivityOptions;->toBundle()Landroid/os/Bundle;
    move-result-object v0
    :try_end_1a1
    .catch Landroid/content/IntentSender$SendIntentException; {:try_start_17f .. :try_end_1a1} :catch_18d
    goto :goto_18b
    :cond_1a2
    move-object v11, v2
    :goto_1a3
    :try_start_1a3
    invoke-virtual {p1}, Landroid/app/PendingIntent;->getIntentSender()Landroid/content/IntentSender;
    move-result-object v5
    iget v6, p0, Lcom/android/billingclient/api/ProxyBillingActivity;->d:I
    new-instance v7, Landroid/content/Intent;
    invoke-direct {v7}, Landroid/content/Intent;-><init>()V
    :try_end_1ae
    .catch Landroid/content/IntentSender$SendIntentException; {:try_start_1a3 .. :try_end_1ae} :catch_1ba
    const/4 v10, 0x0
    const/4 v8, 0x0
    const/4 v9, 0x0
    move-object v4, p0
    :try_start_1b2
    invoke-virtual/range {v4 .. v11}, Landroid/app/Activity;->startIntentSenderForResult(Landroid/content/IntentSender;ILandroid/content/Intent;IIILandroid/os/Bundle;)V
    :try_end_1b5
    .catch Landroid/content/IntentSender$SendIntentException; {:try_start_1b2 .. :try_end_1b5} :catch_1b7
    goto/16 :goto_23b
    :catch_1b7
    move-exception v0
    :goto_1b8
    move-object p1, v0
    goto :goto_1bd
    :catch_1ba
    move-exception v0
    move-object v4, p0
    goto :goto_1b8
    :goto_1bd
    const-string v0, "ProxyBillingActivity"
    const-string v5, "Got exception while trying to start a purchase flow."
    invoke-static {v0, v5, p1}, Lcom/google/android/gms/internal/play_billing/s;->i(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
    iget-object p1, v4, Lcom/android/billingclient/api/ProxyBillingActivity;->a:Landroid/os/ResultReceiver;
    if-eqz p1, :cond_1cc
    invoke-virtual {p1, v1, v2}, Landroid/os/ResultReceiver;->send(ILandroid/os/Bundle;)V
    goto :goto_1e1
    :cond_1cc
    sget-object p1, Lcom/google/android/gms/internal/play_billing/k4;->zzbG:Lcom/google/android/gms/internal/play_billing/k4;
    iget-wide v5, v4, Lcom/android/billingclient/api/ProxyBillingActivity;->e:J
    invoke-virtual {p0, p1, v5, v6, v1}, Lcom/android/billingclient/api/ProxyBillingActivity;->b(Lcom/google/android/gms/internal/play_billing/k4;JZ)Landroid/content/Intent;
    move-result-object p1
    iget-boolean v0, v4, Lcom/android/billingclient/api/ProxyBillingActivity;->c:Z
    if-eqz v0, :cond_1de
    const/4 v0, 0x0
    const-string v0, "IS_FIRST_PARTY_PURCHASE"
    invoke-virtual {p1, v0, v3}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Z)Landroid/content/Intent;
    :cond_1de
    invoke-virtual {p0, p1}, Landroid/content/Context;->sendBroadcast(Landroid/content/Intent;)V
    :goto_1e1
    iput-boolean v1, v4, Lcom/android/billingclient/api/ProxyBillingActivity;->b:Z
    invoke-virtual {p0}, Landroid/app/Activity;->finish()V
    return-void
    :cond_1e7
    move-object v4, p0
    const-string v2, "ProxyBillingActivity"
    const-string v3, "Launching Play Store billing flow from savedInstanceState"
    invoke-static {v2, v3}, Lcom/google/android/gms/internal/play_billing/s;->g(Ljava/lang/String;Ljava/lang/String;)V
    const-string v2, "send_cancelled_broadcast_if_finished"
    invoke-virtual {p1, v2, v1}, Landroid/os/BaseBundle;->getBoolean(Ljava/lang/String;Z)Z
    move-result v2
    iput-boolean v2, v4, Lcom/android/billingclient/api/ProxyBillingActivity;->b:Z
    const/4 v2, 0x0
    const-string v2, "in_app_message_result_receiver"
    invoke-virtual {p1, v2}, Landroid/os/BaseBundle;->containsKey(Ljava/lang/String;)Z
    move-result v2
    if-eqz v2, :cond_20b
    const/4 v2, 0x0
    const-string v2, "in_app_message_result_receiver"
    invoke-virtual {p1, v2}, Landroid/os/Bundle;->getParcelable(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v2
    check-cast v2, Landroid/os/ResultReceiver;
    iput-object v2, v4, Lcom/android/billingclient/api/ProxyBillingActivity;->a:Landroid/os/ResultReceiver;
    :cond_20b
    const-string v2, "IS_FLOW_FROM_FIRST_PARTY_CLIENT"
    invoke-virtual {p1, v2, v1}, Landroid/os/BaseBundle;->getBoolean(Ljava/lang/String;Z)Z
    move-result v1
    iput-boolean v1, v4, Lcom/android/billingclient/api/ProxyBillingActivity;->c:Z
    const-string v1, "activity_code"
    invoke-virtual {p1, v1, v0}, Landroid/os/BaseBundle;->getInt(Ljava/lang/String;I)I
    move-result v0
    iput v0, v4, Lcom/android/billingclient/api/ProxyBillingActivity;->d:I
    const-string v0, "billingClientTransactionId"
    invoke-virtual {p1, v0}, Landroid/os/BaseBundle;->containsKey(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :cond_22b
    const-string v0, "billingClientTransactionId"
    invoke-virtual {p1, v0}, Landroid/os/BaseBundle;->getLong(Ljava/lang/String;)J
    move-result-wide v0
    iput-wide v0, v4, Lcom/android/billingclient/api/ProxyBillingActivity;->e:J
    :cond_22b
    const-string v0, "wasServiceAutoReconnected"
    invoke-virtual {p1, v0}, Landroid/os/BaseBundle;->containsKey(Ljava/lang/String;)Z
    move-result v0
    if-eqz v0, :cond_23b
    const-string v0, "wasServiceAutoReconnected"
    invoke-virtual {p1, v0}, Landroid/os/BaseBundle;->getBoolean(Ljava/lang/String;)Z
    move-result p1
    iput-boolean p1, v4, Lcom/android/billingclient/api/ProxyBillingActivity;->f:Z
    :cond_23b
    :goto_23b
    return-void
