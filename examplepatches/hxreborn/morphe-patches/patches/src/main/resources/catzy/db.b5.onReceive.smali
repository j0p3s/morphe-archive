    const/4 v0, 0x0
    const/4 v1, 0x1
    iget-object v2, p0, Ldb/b5;->b:Ljava/lang/Object;
    iget v3, p0, Ldb/b5;->a:I
    packed-switch v3, :pswitch_data_170
    new-instance p1, Landroid/content/Intent;
    check-cast v2, Lcom/facebook/CustomTabMainActivity;
    const-class v0, Lcom/facebook/CustomTabMainActivity;
    invoke-direct {p1, v2, v0}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
    sget v0, Lcom/facebook/CustomTabMainActivity;->c:I
    const-string v0, "CustomTabMainActivity.action_refresh"
    invoke-virtual {p1, v0}, Landroid/content/Intent;->setAction(Ljava/lang/String;)Landroid/content/Intent;
    const-string v0, "CustomTabMainActivity.extra_url"
    invoke-virtual {p2, v0}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;
    move-result-object p2
    invoke-virtual {p1, v0, p2}, Landroid/content/Intent;->putExtra(Ljava/lang/String;Ljava/lang/String;)Landroid/content/Intent;
    const/high16 p2, 0x24000000
    invoke-virtual {p1, p2}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;
    invoke-virtual {v2, p1}, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V
    return-void
    :pswitch_2b
    check-cast v2, Lcom/facebook/CustomTabActivity;
    invoke-virtual {v2}, Landroid/app/Activity;->finish()V
    return-void
    :pswitch_31
    invoke-virtual {p0}, Landroid/content/BroadcastReceiver;->isInitialStickyBroadcast()Z
    move-result v0
    if-nez v0, :cond_44
    check-cast v2, Ln2/j;
    iget-object v0, v2, Ln2/j;->i:Landroidx/media3/common/g;
    iget-object v1, v2, Ln2/j;->h:Ln2/k;
    invoke-static {p1, p2, v0, v1}, Ln2/f;->b(Landroid/content/Context;Landroid/content/Intent;Landroidx/media3/common/g;Ln2/k;)Ln2/f;
    move-result-object p1
    invoke-virtual {v2, p1}, Ln2/j;->a(Ln2/f;)V
    :cond_44
    return-void
    :pswitch_45
    check-cast v2, Landroidx/credentials/g;
    invoke-virtual {v2}, Landroidx/credentials/g;->o()V
    return-void
    :pswitch_4b
    const-string p2, "connectivity"
    invoke-virtual {p1, p2}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object p2
    check-cast p2, Landroid/net/ConnectivityManager;
    const/4 v3, 0x5
    if-nez p2, :cond_57
    goto :goto_97
    :cond_57
    :try_start_57
    invoke-virtual {p2}, Landroid/net/ConnectivityManager;->getActiveNetworkInfo()Landroid/net/NetworkInfo;
    move-result-object p2
    :try_end_5b
    .catch Ljava/lang/SecurityException; {:try_start_57 .. :try_end_5b} :catch_97
    if-eqz p2, :cond_96
    invoke-virtual {p2}, Landroid/net/NetworkInfo;->isConnected()Z
    move-result v4
    if-nez v4, :cond_64
    goto :goto_96
    :cond_64
    invoke-virtual {p2}, Landroid/net/NetworkInfo;->getType()I
    move-result v4
    const/16 v5, 0x9
    const/4 v6, 0x6
    const/4 v7, 0x4
    if-eqz v4, :cond_7d
    if-eq v4, v1, :cond_8e
    if-eq v4, v7, :cond_7d
    if-eq v4, v3, :cond_7d
    if-eq v4, v6, :cond_90
    if-eq v4, v5, :cond_7b
    const/16 v0, 0x8
    goto :goto_97
    :cond_7b
    const/4 v0, 0x7
    goto :goto_97
    :cond_7d
    invoke-virtual {p2}, Landroid/net/NetworkInfo;->getSubtype()I
    move-result p2
    packed-switch p2, :pswitch_data_17e
    :pswitch_84
    move v0, v6
    goto :goto_97
    :pswitch_86
    sget p2, Li2/v;->a:I
    const/16 v1, 0x1d
    if-lt p2, v1, :cond_97
    move v0, v5
    goto :goto_97
    :cond_8e
    :pswitch_8e
    const/4 v0, 0x2
    goto :goto_97
    :cond_90
    :pswitch_90
    move v0, v3
    goto :goto_97
    :pswitch_92
    move v0, v7
    goto :goto_97
    :pswitch_94
    const/4 v0, 0x3
    goto :goto_97
    :cond_96
    :goto_96
    move v0, v1
    :catch_97
    :cond_97
    :goto_97
    sget p2, Li2/v;->a:I
    const/16 v1, 0x1f
    check-cast v2, Li2/l;
    if-lt p2, v1, :cond_c0
    if-ne v0, v3, :cond_c0
    :try_start_a1
    const-string p2, "phone"
    invoke-virtual {p1, p2}, Landroid/content/Context;->getSystemService(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object p2
    check-cast p2, Landroid/telephony/TelephonyManager;
    invoke-virtual {p2}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    new-instance v0, Li2/k;
    invoke-direct {v0, v2}, Li2/k;-><init>(Li2/l;)V
    invoke-static {p1}, La1/c;->m(Landroid/content/Context;)Ljava/util/concurrent/Executor;
    move-result-object p1
    invoke-static {p2, p1, v0}, Landroidx/core/view/c;->u(Landroid/telephony/TelephonyManager;Ljava/util/concurrent/Executor;Li2/k;)V
    invoke-static {p2, v0}, Landroidx/core/view/c;->t(Landroid/telephony/TelephonyManager;Li2/k;)V
    :try_end_bb
    .catch Ljava/lang/RuntimeException; {:try_start_a1 .. :try_end_bb} :catch_bc
    goto :goto_c3
    :catch_bc
    invoke-static {v2, v3}, Li2/l;->d(Li2/l;I)V
    goto :goto_c3
    :cond_c0
    invoke-static {v2, v0}, Li2/l;->d(Li2/l;I)V
    :goto_c3
    return-void
    :pswitch_c4
    return-void
    :cond_16e
    :goto_16e
    return-void
    nop
    :pswitch_data_170
    .packed-switch 0x0
        :pswitch_c4
        :pswitch_45
        :pswitch_4b
        :pswitch_31
        :pswitch_2b
    .end packed-switch
    :pswitch_data_17e
    .packed-switch 0x1
        :pswitch_94
        :pswitch_94
        :pswitch_92
        :pswitch_92
        :pswitch_92
        :pswitch_92
        :pswitch_92
        :pswitch_92
        :pswitch_92
        :pswitch_92
        :pswitch_92
        :pswitch_92
        :pswitch_90
        :pswitch_92
        :pswitch_92
        :pswitch_84
        :pswitch_92
        :pswitch_8e
        :pswitch_84
        :pswitch_86
    .end packed-switch
