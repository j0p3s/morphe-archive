    invoke-super {p0, p1}, Landroidx/activity/t;->onCreate(Landroid/os/Bundle;)V
    new-instance v0, Lg/i;
    const/4 v1, 0x1
    invoke-direct {v0, v1}, Lg/i;-><init>(I)V
    new-instance v1, Lg6/h0;
    const/4 v2, 0x0
    invoke-direct {v1, p0, v2}, Lg6/h0;-><init>(Lcom/android/billingclient/api/ProxyBillingActivityV2;I)V
    invoke-virtual {p0, v0, v1}, Landroidx/activity/t;->registerForActivityResult(Lg/a;Lf/b;)Lf/c;
    move-result-object v0
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->a:Lf/c;
    new-instance v0, Lg/i;
    const/4 v1, 0x1
    invoke-direct {v0, v1}, Lg/i;-><init>(I)V
    new-instance v1, Lg6/i0;
    const/4 v2, 0x0
    invoke-direct {v1, p0, v2}, Lg6/i0;-><init>(Lcom/android/billingclient/api/ProxyBillingActivityV2;I)V
    invoke-virtual {p0, v0, v1}, Landroidx/activity/t;->registerForActivityResult(Lg/a;Lf/b;)Lf/c;
    move-result-object v0
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->b:Lf/c;
    new-instance v0, Lg/i;
    const/4 v1, 0x1
    invoke-direct {v0, v1}, Lg/i;-><init>(I)V
    new-instance v1, Lg6/j0;
    const/4 v2, 0x0
    invoke-direct {v1, p0, v2}, Lg6/j0;-><init>(Lcom/android/billingclient/api/ProxyBillingActivityV2;I)V
    invoke-virtual {p0, v0, v1}, Landroidx/activity/t;->registerForActivityResult(Lg/a;Lf/b;)Lf/c;
    move-result-object v0
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->c:Lf/c;
    new-instance v0, Lg/i;
    const/4 v1, 0x1
    invoke-direct {v0, v1}, Lg/i;-><init>(I)V
    new-instance v1, Lg6/h0;
    const/4 v2, 0x1
    invoke-direct {v1, p0, v2}, Lg6/h0;-><init>(Lcom/android/billingclient/api/ProxyBillingActivityV2;I)V
    invoke-virtual {p0, v0, v1}, Landroidx/activity/t;->registerForActivityResult(Lg/a;Lf/b;)Lf/c;
    move-result-object v0
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->d:Lf/c;
    new-instance v0, Lg/i;
    const/4 v1, 0x1
    invoke-direct {v0, v1}, Lg/i;-><init>(I)V
    new-instance v1, Lg6/i0;
    const/4 v2, 0x1
    invoke-direct {v1, p0, v2}, Lg6/i0;-><init>(Lcom/android/billingclient/api/ProxyBillingActivityV2;I)V
    invoke-virtual {p0, v0, v1}, Landroidx/activity/t;->registerForActivityResult(Lg/a;Lf/b;)Lf/c;
    move-result-object v0
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->e:Lf/c;
    new-instance v0, Lg/i;
    const/4 v1, 0x1
    invoke-direct {v0, v1}, Lg/i;-><init>(I)V
    new-instance v1, Lg6/j0;
    const/4 v2, 0x1
    invoke-direct {v1, p0, v2}, Lg6/j0;-><init>(Lcom/android/billingclient/api/ProxyBillingActivityV2;I)V
    invoke-virtual {p0, v0, v1}, Landroidx/activity/t;->registerForActivityResult(Lg/a;Lf/b;)Lf/c;
    move-result-object v0
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->f:Lf/c;
    const-string v0, "subscription_management_action_result_receiver"
    const-string v1, "billing_program_information_dialog_result_receiver"
    const-string v2, "launch_external_link_result_receiver"
    const-string v3, "external_offer_flow_result_receiver"
    const-string v4, "external_payment_dialog_result_receiver"
    const-string v5, "alternative_billing_only_dialog_result_receiver"
    if-nez p1, :cond_1c4
    const-string p1, "ProxyBillingActivityV2"
    const-string v6, "Launching Play Store billing dialog"
    invoke-static {p1, v6}, Lcom/google/android/gms/internal/play_billing/s;->g(Ljava/lang/String;Ljava/lang/String;)V
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v6, "ALTERNATIVE_BILLING_ONLY_DIALOG_INTENT"
    invoke-virtual {p1, v6}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z
    move-result p1
    const/4 v7, 0x0
    const/4 v8, 0x0
    if-eqz p1, :cond_bb
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    invoke-virtual {p1, v6}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object p1
    check-cast p1, Landroid/app/PendingIntent;
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    invoke-virtual {v0, v5}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v0
    check-cast v0, Landroid/os/ResultReceiver;
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->g:Landroid/os/ResultReceiver;
    iget-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->a:Lf/c;
    invoke-virtual {p1}, Landroid/app/PendingIntent;->getIntentSender()Landroid/content/IntentSender;
    move-result-object p1
    new-instance v1, Lf/m;
    invoke-direct {v1, p1, v7, v8, v8}, Lf/m;-><init>(Landroid/content/IntentSender;Landroid/content/Intent;II)V
    invoke-static {}, Lcom/android/billingclient/api/ProxyBillingActivityV2;->Z()Lcom/nieruo/healthapp/views/s1;
    move-result-object p1
    invoke-virtual {v0, v1, p1}, Lf/c;->a(Ljava/lang/Object;Lcom/nieruo/healthapp/views/s1;)V
    return-void
    :cond_bb
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v5, "external_payment_dialog_pending_intent"
    invoke-virtual {p1, v5}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z
    move-result p1
    if-eqz p1, :cond_f0
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    invoke-virtual {p1, v5}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object p1
    check-cast p1, Landroid/app/PendingIntent;
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    invoke-virtual {v0, v4}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v0
    check-cast v0, Landroid/os/ResultReceiver;
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->h:Landroid/os/ResultReceiver;
    iget-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->b:Lf/c;
    invoke-virtual {p1}, Landroid/app/PendingIntent;->getIntentSender()Landroid/content/IntentSender;
    move-result-object p1
    new-instance v1, Lf/m;
    invoke-direct {v1, p1, v7, v8, v8}, Lf/m;-><init>(Landroid/content/IntentSender;Landroid/content/Intent;II)V
    invoke-static {}, Lcom/android/billingclient/api/ProxyBillingActivityV2;->Z()Lcom/nieruo/healthapp/views/s1;
    move-result-object p1
    invoke-virtual {v0, v1, p1}, Lf/c;->a(Ljava/lang/Object;Lcom/nieruo/healthapp/views/s1;)V
    return-void
    :cond_f0
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v4, "external_offer_flow_pending_intent"
    invoke-virtual {p1, v4}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z
    move-result p1
    if-eqz p1, :cond_125
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    invoke-virtual {p1, v4}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object p1
    check-cast p1, Landroid/app/PendingIntent;
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    invoke-virtual {v0, v3}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v0
    check-cast v0, Landroid/os/ResultReceiver;
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->i:Landroid/os/ResultReceiver;
    iget-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->c:Lf/c;
    invoke-virtual {p1}, Landroid/app/PendingIntent;->getIntentSender()Landroid/content/IntentSender;
    move-result-object p1
    new-instance v1, Lf/m;
    invoke-direct {v1, p1, v7, v8, v8}, Lf/m;-><init>(Landroid/content/IntentSender;Landroid/content/Intent;II)V
    invoke-static {}, Lcom/android/billingclient/api/ProxyBillingActivityV2;->Z()Lcom/nieruo/healthapp/views/s1;
    move-result-object p1
    invoke-virtual {v0, v1, p1}, Lf/c;->a(Ljava/lang/Object;Lcom/nieruo/healthapp/views/s1;)V
    return-void
    :cond_125
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v3, "launch_external_link_flow_pending_intent"
    invoke-virtual {p1, v3}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z
    move-result p1
    if-eqz p1, :cond_15a
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    invoke-virtual {p1, v3}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object p1
    check-cast p1, Landroid/app/PendingIntent;
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    invoke-virtual {v0, v2}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v0
    check-cast v0, Landroid/os/ResultReceiver;
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->j:Landroid/os/ResultReceiver;
    iget-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->d:Lf/c;
    invoke-virtual {p1}, Landroid/app/PendingIntent;->getIntentSender()Landroid/content/IntentSender;
    move-result-object p1
    new-instance v1, Lf/m;
    invoke-direct {v1, p1, v7, v8, v8}, Lf/m;-><init>(Landroid/content/IntentSender;Landroid/content/Intent;II)V
    invoke-static {}, Lcom/android/billingclient/api/ProxyBillingActivityV2;->Z()Lcom/nieruo/healthapp/views/s1;
    move-result-object p1
    invoke-virtual {v0, v1, p1}, Lf/c;->a(Ljava/lang/Object;Lcom/nieruo/healthapp/views/s1;)V
    return-void
    :cond_15a
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v2, "billing_program_information_dialog_pending_intent"
    invoke-virtual {p1, v2}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z
    move-result p1
    if-eqz p1, :cond_18f
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    invoke-virtual {p1, v2}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object p1
    check-cast p1, Landroid/app/PendingIntent;
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v0
    invoke-virtual {v0, v1}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v0
    check-cast v0, Landroid/os/ResultReceiver;
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->k:Landroid/os/ResultReceiver;
    iget-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->e:Lf/c;
    invoke-virtual {p1}, Landroid/app/PendingIntent;->getIntentSender()Landroid/content/IntentSender;
    move-result-object p1
    new-instance v1, Lf/m;
    invoke-direct {v1, p1, v7, v8, v8}, Lf/m;-><init>(Landroid/content/IntentSender;Landroid/content/Intent;II)V
    invoke-static {}, Lcom/android/billingclient/api/ProxyBillingActivityV2;->Z()Lcom/nieruo/healthapp/views/s1;
    move-result-object p1
    invoke-virtual {v0, v1, p1}, Lf/c;->a(Ljava/lang/Object;Lcom/nieruo/healthapp/views/s1;)V
    return-void
    :cond_18f
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v1, "SUBSCRIPTION_MANAGEMENT_INTENT"
    invoke-virtual {p1, v1}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z
    move-result p1
    if-eqz p1, :cond_218
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    invoke-virtual {p1, v1}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object p1
    check-cast p1, Landroid/app/PendingIntent;
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v1
    invoke-virtual {v1, v0}, Landroid/content/Intent;->getParcelableExtra(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v0
    check-cast v0, Landroid/os/ResultReceiver;
    iput-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->l:Landroid/os/ResultReceiver;
    iget-object v0, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->f:Lf/c;
    invoke-virtual {p1}, Landroid/app/PendingIntent;->getIntentSender()Landroid/content/IntentSender;
    move-result-object p1
    new-instance v1, Lf/m;
    invoke-direct {v1, p1, v7, v8, v8}, Lf/m;-><init>(Landroid/content/IntentSender;Landroid/content/Intent;II)V
    invoke-static {}, Lcom/android/billingclient/api/ProxyBillingActivityV2;->Z()Lcom/nieruo/healthapp/views/s1;
    move-result-object p1
    invoke-virtual {v0, v1, p1}, Lf/c;->a(Ljava/lang/Object;Lcom/nieruo/healthapp/views/s1;)V
    return-void
    :cond_1c4
    invoke-virtual {p1, v5}, Landroid/os/BaseBundle;->containsKey(Ljava/lang/String;)Z
    move-result v6
    if-eqz v6, :cond_1d2
    invoke-virtual {p1, v5}, Landroid/os/Bundle;->getParcelable(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v5
    check-cast v5, Landroid/os/ResultReceiver;
    iput-object v5, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->g:Landroid/os/ResultReceiver;
    :cond_1d2
    invoke-virtual {p1, v4}, Landroid/os/BaseBundle;->containsKey(Ljava/lang/String;)Z
    move-result v5
    if-eqz v5, :cond_1e0
    invoke-virtual {p1, v4}, Landroid/os/Bundle;->getParcelable(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v4
    check-cast v4, Landroid/os/ResultReceiver;
    iput-object v4, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->h:Landroid/os/ResultReceiver;
    :cond_1e0
    invoke-virtual {p1, v3}, Landroid/os/BaseBundle;->containsKey(Ljava/lang/String;)Z
    move-result v4
    if-eqz v4, :cond_1ee
    invoke-virtual {p1, v3}, Landroid/os/Bundle;->getParcelable(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v3
    check-cast v3, Landroid/os/ResultReceiver;
    iput-object v3, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->i:Landroid/os/ResultReceiver;
    :cond_1ee
    invoke-virtual {p1, v2}, Landroid/os/BaseBundle;->containsKey(Ljava/lang/String;)Z
    move-result v3
    if-eqz v3, :cond_1fc
    invoke-virtual {p1, v2}, Landroid/os/Bundle;->getParcelable(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v2
    check-cast v2, Landroid/os/ResultReceiver;
    iput-object v2, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->j:Landroid/os/ResultReceiver;
    :cond_1fc
    invoke-virtual {p1, v1}, Landroid/os/BaseBundle;->containsKey(Ljava/lang/String;)Z
    move-result v2
    if-eqz v2, :cond_20a
    invoke-virtual {p1, v1}, Landroid/os/Bundle;->getParcelable(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object v1
    check-cast v1, Landroid/os/ResultReceiver;
    iput-object v1, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->k:Landroid/os/ResultReceiver;
    :cond_20a
    invoke-virtual {p1, v0}, Landroid/os/BaseBundle;->containsKey(Ljava/lang/String;)Z
    move-result v1
    if-eqz v1, :cond_218
    invoke-virtual {p1, v0}, Landroid/os/Bundle;->getParcelable(Ljava/lang/String;)Landroid/os/Parcelable;
    move-result-object p1
    check-cast p1, Landroid/os/ResultReceiver;
    iput-object p1, p0, Lcom/android/billingclient/api/ProxyBillingActivityV2;->l:Landroid/os/ResultReceiver;
    :cond_218
    return-void
