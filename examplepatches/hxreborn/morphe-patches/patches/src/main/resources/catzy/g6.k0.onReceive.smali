    const-string p1, "ProxyBillingReceiver"
    if-nez p2, :cond_a
    const-string p2, "Null intent!"
    invoke-static {p1, p2}, Lcom/google/android/gms/internal/play_billing/s;->h(Ljava/lang/String;Ljava/lang/String;)V
    return-void
    :cond_a
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;
    move-result-object v0
    invoke-static {v0}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
    move-result-object v0
    const-string v1, "Received intent action: "
    invoke-virtual {v1, v0}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v0
    invoke-static {p1, v0}, Lcom/google/android/gms/internal/play_billing/s;->g(Ljava/lang/String;Ljava/lang/String;)V
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;
    move-result-object v0
    const-string v1, "com.android.vending.billing.IN_APP_BILLING_RESULT_UPDATE_ACTION"
    invoke-static {v0, v1}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v0
    const-string v1, "billingClientTransactionId"
    iget-object v2, p0, Lg6/k0;->c:Landroidx/work/impl/model/c;
    const-wide/16 v3, 0x0
    if-eqz v0, :cond_6c
    const-string v0, "RESPONSE_CODE"
    invoke-virtual {p2, v0}, Landroid/content/Intent;->hasExtra(Ljava/lang/String;)Z
    move-result v5
    if-nez v5, :cond_45
    const-string v0, "Missing RESPONSE_CODE in intent."
    invoke-static {p1, v0}, Lcom/google/android/gms/internal/play_billing/s;->h(Ljava/lang/String;Ljava/lang/String;)V
    if-eqz v2, :cond_d7
    invoke-virtual {p2, v1, v3, v4}, Landroid/content/Intent;->getLongExtra(Ljava/lang/String;J)J
    move-result-wide p1
    const/4 v0, 0x0
    invoke-virtual {v2, p1, p2, v0}, Landroidx/work/impl/model/c;->Q0(JLg6/h;)V
    return-void
    :cond_45
    invoke-static {}, Lg6/h;->a()Lfa/r;
    move-result-object p1
    const/4 v5, 0x0
    invoke-virtual {p2, v0, v5}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I
    move-result v0
    iput v0, p1, Lfa/r;->a:I
    const-string v0, "DEBUG_MESSAGE"
    invoke-virtual {p2, v0}, Landroid/content/Intent;->getStringExtra(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v0
    if-nez v0, :cond_5a
    const-string v0, ""
    :cond_5a
    iput-object v0, p1, Lfa/r;->c:Ljava/lang/Object;
    invoke-virtual {p1}, Lfa/r;->a()Lg6/h;
    move-result-object p1
    iput-object p1, p0, Lg6/k0;->a:Lg6/h;
    if-eqz v2, :cond_d7
    invoke-virtual {p2, v1, v3, v4}, Landroid/content/Intent;->getLongExtra(Ljava/lang/String;J)J
    move-result-wide v0
    invoke-virtual {v2, v0, v1, p1}, Landroidx/work/impl/model/c;->Q0(JLg6/h;)V
    return-void
    :cond_6c
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;
    move-result-object v0
    const-string v5, "com.android.vending.billing.PLAY_BILLING_ACTIVITY_CREATED_ACTION"
    invoke-static {v0, v5}, Ljava/util/Objects;->equals(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :cond_d8
    const/4 p1, 0x1
    iput-boolean p1, p0, Lg6/k0;->b:Z
    if-eqz v2, :cond_d7
    invoke-virtual {p2, v1, v3, v4}, Landroid/content/Intent;->getLongExtra(Ljava/lang/String;J)J
    move-result-wide p1
    :try_start_81
    invoke-static {}, Lcom/google/android/gms/internal/play_billing/p4;->r()Lcom/google/android/gms/internal/play_billing/o4;
    move-result-object v0
    const/4 v1, 0x4
    invoke-virtual {v0, v1}, Lcom/google/android/gms/internal/play_billing/o4;->l(I)V
    sget-object v1, Lcom/google/android/gms/internal/play_billing/n4;->zzf:Lcom/google/android/gms/internal/play_billing/n4;
    invoke-virtual {v0, v1}, Lcom/google/android/gms/internal/play_billing/o4;->g(Lcom/google/android/gms/internal/play_billing/n4;)V
    invoke-virtual {v0}, Lcom/google/android/gms/internal/play_billing/m2;->b()Lcom/google/android/gms/internal/play_billing/n2;
    move-result-object v0
    check-cast v0, Lcom/google/android/gms/internal/play_billing/p4;
    invoke-static {}, Lcom/google/android/gms/internal/play_billing/y4;->t()Lcom/google/android/gms/internal/play_billing/x4;
    move-result-object v1
    cmp-long v3, p1, v3
    if-nez v3, :cond_a1
    iget-object p1, v2, Landroidx/work/impl/model/c;->b:Ljava/lang/Object;
    check-cast p1, Lcom/google/android/gms/internal/play_billing/r4;
    goto :goto_b4
    :cond_a1
    iget-object v3, v2, Landroidx/work/impl/model/c;->b:Ljava/lang/Object;
    check-cast v3, Lcom/google/android/gms/internal/play_billing/r4;
    invoke-virtual {v3}, Lcom/google/android/gms/internal/play_billing/n2;->l()Lcom/google/android/gms/internal/play_billing/m2;
    move-result-object v3
    check-cast v3, Lcom/google/android/gms/internal/play_billing/q4;
    invoke-virtual {v3, p1, p2}, Lcom/google/android/gms/internal/play_billing/q4;->i(J)V
    invoke-virtual {v3}, Lcom/google/android/gms/internal/play_billing/m2;->b()Lcom/google/android/gms/internal/play_billing/n2;
    move-result-object p1
    check-cast p1, Lcom/google/android/gms/internal/play_billing/r4;
    :goto_b4
    invoke-virtual {v1, p1}, Lcom/google/android/gms/internal/play_billing/x4;->g(Lcom/google/android/gms/internal/play_billing/r4;)V
    invoke-virtual {v1}, Lcom/google/android/gms/internal/play_billing/m2;->d()V
    iget-object p1, v1, Lcom/google/android/gms/internal/play_billing/m2;->b:Lcom/google/android/gms/internal/play_billing/n2;
    check-cast p1, Lcom/google/android/gms/internal/play_billing/y4;
    invoke-static {p1, v0}, Lcom/google/android/gms/internal/play_billing/y4;->x(Lcom/google/android/gms/internal/play_billing/y4;Lcom/google/android/gms/internal/play_billing/p4;)V
    invoke-virtual {v1}, Lcom/google/android/gms/internal/play_billing/m2;->b()Lcom/google/android/gms/internal/play_billing/n2;
    move-result-object p1
    check-cast p1, Lcom/google/android/gms/internal/play_billing/y4;
    iget-object p2, v2, Landroidx/work/impl/model/c;->c:Ljava/lang/Object;
    check-cast p2, Lcn/thinkingdata/analytics/autotrack/d;
    invoke-virtual {p2, p1}, Lcn/thinkingdata/analytics/autotrack/d;->c(Lcom/google/android/gms/internal/play_billing/y4;)V
    :try_end_ce
    .catchall {:try_start_81 .. :try_end_ce} :catchall_cf
    return-void
    :catchall_cf
    move-exception p1
    const-string p2, "BillingLogger"
    const-string v0, "Unable to log."
    invoke-static {p2, v0, p1}, Lcom/google/android/gms/internal/play_billing/s;->i(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
    :cond_d7
    return-void
    :cond_d8
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;
    move-result-object p2
    invoke-static {p2}, Ljava/lang/String;->valueOf(Ljava/lang/Object;)Ljava/lang/String;
    move-result-object p2
    const-string v0, "Unexpected broadcast action: "
    invoke-virtual {v0, p2}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
    move-result-object p2
    invoke-static {p1, p2}, Lcom/google/android/gms/internal/play_billing/s;->h(Ljava/lang/String;Ljava/lang/String;)V
    return-void
