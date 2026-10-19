    const/4 v0, 0x1
    invoke-virtual/range {p2 .. p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;
    move-result-object v1
    invoke-virtual {v1}, Ljava/lang/String;->hashCode()I
    move-result v2
    const v3, -0x58756162
    const/4 v4, 0x2
    const/4 v5, 0x0
    if-eq v2, v3, :cond_2f
    const v3, -0x141f9074
    if-eq v2, v3, :cond_25
    const v3, 0x14937179
    if-eq v2, v3, :cond_1b
    goto :goto_39
    :cond_1b
    const-string v2, "com.android.vending.billing.ALTERNATIVE_BILLING"
    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :cond_39
    move v1, v4
    goto :goto_3a
    :cond_25
    const-string v2, "com.android.vending.billing.LOCAL_BROADCAST_PURCHASES_UPDATED"
    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :cond_39
    move v1, v0
    goto :goto_3a
    :cond_2f
    const-string v2, "com.android.vending.billing.PURCHASES_UPDATED"
    invoke-virtual {v1, v2}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :cond_39
    move v1, v5
    goto :goto_3a
    :cond_39
    :goto_39
    const/4 v1, -0x1
    :goto_3a
    if-eqz v1, :cond_4a
    if-eq v1, v0, :cond_47
    if-eq v1, v4, :cond_44
    sget-object v1, Lcom/google/android/gms/internal/play_billing/n4;->zza:Lcom/google/android/gms/internal/play_billing/n4;
    :goto_42
    move-object v10, v1
    goto :goto_4d
    :cond_44
    sget-object v1, Lcom/google/android/gms/internal/play_billing/n4;->zzd:Lcom/google/android/gms/internal/play_billing/n4;
    goto :goto_42
    :cond_47
    sget-object v1, Lcom/google/android/gms/internal/play_billing/n4;->zzc:Lcom/google/android/gms/internal/play_billing/n4;
    goto :goto_42
    :cond_4a
    sget-object v1, Lcom/google/android/gms/internal/play_billing/n4;->zzb:Lcom/google/android/gms/internal/play_billing/n4;
    goto :goto_42
    :goto_4d
    sget-object v1, Lcom/google/android/gms/internal/play_billing/n4;->zzc:Lcom/google/android/gms/internal/play_billing/n4;
    invoke-virtual {v10, v1}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z
    move-result v2
    if-nez v2, :cond_5d
    sget-object v2, Lcom/google/android/gms/internal/play_billing/n4;->zzd:Lcom/google/android/gms/internal/play_billing/n4;
    invoke-virtual {v10, v2}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :cond_5f
    :cond_5d
    move v9, v4
    goto :goto_6c
    :cond_5f
    sget-object v2, Lcom/google/android/gms/internal/play_billing/n4;->zzb:Lcom/google/android/gms/internal/play_billing/n4;
    invoke-virtual {v10, v2}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :cond_6b
    const/16 v2, 0x20
    move v9, v2
    goto :goto_6c
    :cond_6b
    move v9, v0
    :goto_6c
    invoke-virtual/range {p2 .. p2}, Landroid/content/Intent;->getExtras()Landroid/os/Bundle;
    move-result-object v7
    const/4 v2, 0x0
    move-object/from16 v6, p0
    iget-object v3, v6, Lg6/m0;->c:Lg6/n0;
    const-string v14, "BillingBroadcastManager"
    if-nez v7, :cond_93
    const-string v0, "Bundle is null."
    invoke-static {v14, v0}, Lcom/google/android/gms/internal/play_billing/s;->h(Ljava/lang/String;Ljava/lang/String;)V
    iget-object v0, v3, Lg6/n0;->c:Landroidx/work/impl/model/c;
    sget-object v1, Lcom/google/android/gms/internal/play_billing/k4;->zzk:Lcom/google/android/gms/internal/play_billing/k4;
    sget-object v4, Lg6/e0;->f:Lg6/h;
    invoke-static {v1, v9, v4, v2, v10}, Lg6/c0;->b(Lcom/google/android/gms/internal/play_billing/k4;ILg6/h;Ljava/lang/String;Lcom/google/android/gms/internal/play_billing/n4;)Lcom/google/android/gms/internal/play_billing/f4;
    move-result-object v1
    invoke-virtual {v0, v1}, Landroidx/work/impl/model/c;->K0(Lcom/google/android/gms/internal/play_billing/f4;)V
    iget-object v0, v3, Lg6/n0;->b:Landroidx/work/impl/o;
    if-eqz v0, :cond_151
    invoke-virtual {v0, v4, v2}, Landroidx/work/impl/o;->c(Lg6/h;Ljava/util/List;)V
    return-void
    :cond_93
    if-ne v9, v4, :cond_ef
    sget v4, Lcom/google/android/gms/internal/play_billing/s;->a:I
    invoke-static {}, Lg6/h;->a()Lfa/r;
    move-result-object v4
    invoke-virtual/range {p2 .. p2}, Landroid/content/Intent;->getExtras()Landroid/os/Bundle;
    move-result-object v8
    invoke-static {v14, v8}, Lcom/google/android/gms/internal/play_billing/s;->a(Ljava/lang/String;Landroid/os/Bundle;)I
    move-result v8
    iput v8, v4, Lfa/r;->a:I
    invoke-virtual/range {p2 .. p2}, Landroid/content/Intent;->getExtras()Landroid/os/Bundle;
    move-result-object v8
    if-nez v8, :cond_b2
    const-string v8, "Unexpected null bundle received!"
    invoke-static {v14, v8}, Lcom/google/android/gms/internal/play_billing/s;->h(Ljava/lang/String;Ljava/lang/String;)V
    :goto_b0
    move v8, v5
    goto :goto_dd
    :cond_b2
    const-string v11, "SUB_RESPONSE_CODE"
    invoke-virtual {v8, v11}, Landroid/os/BaseBundle;->get(Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v8
    if-nez v8, :cond_c0
    const-string v8, "getOnPurchasesUpdatedSubResponseCodeFromBundle() got null response code, assuming OK"
    invoke-static {v14, v8}, Lcom/google/android/gms/internal/play_billing/s;->g(Ljava/lang/String;Ljava/lang/String;)V
    goto :goto_b0
    :cond_c0
    instance-of v11, v8, Ljava/lang/Integer;
    if-eqz v11, :cond_cb
    check-cast v8, Ljava/lang/Integer;
    invoke-virtual {v8}, Ljava/lang/Integer;->intValue()I
    move-result v8
    goto :goto_dd
    :cond_cb
    invoke-virtual {v8}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    move-result-object v8
    invoke-virtual {v8}, Ljava/lang/Class;->getName()Ljava/lang/String;
    move-result-object v8
    const-string v11, "Unexpected type for bundle sub response code: "
    invoke-virtual {v11, v8}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v8
    invoke-static {v14, v8}, Lcom/google/android/gms/internal/play_billing/s;->h(Ljava/lang/String;Ljava/lang/String;)V
    goto :goto_b0
    :goto_dd
    iput v8, v4, Lfa/r;->b:I
    invoke-virtual/range {p2 .. p2}, Landroid/content/Intent;->getExtras()Landroid/os/Bundle;
    move-result-object v8
    invoke-static {v14, v8}, Lcom/google/android/gms/internal/play_billing/s;->f(Ljava/lang/String;Landroid/os/Bundle;)Ljava/lang/String;
    move-result-object v8
    iput-object v8, v4, Lfa/r;->c:Ljava/lang/Object;
    invoke-virtual {v4}, Lfa/r;->a()Lg6/h;
    move-result-object v4
    :goto_ed
    move-object v8, v4
    goto :goto_f6
    :cond_ef
    move-object/from16 v4, p2
    invoke-static {v14, v4}, Lcom/google/android/gms/internal/play_billing/s;->e(Ljava/lang/String;Landroid/content/Intent;)Lg6/h;
    move-result-object v4
    goto :goto_ed
    :goto_f6
    const-string v4, "billingClientTransactionId"
    const-wide/16 v11, 0x0
    invoke-virtual {v7, v4, v11, v12}, Landroid/os/BaseBundle;->getLong(Ljava/lang/String;J)J
    move-result-wide v15
    const-string v4, "wasServiceAutoReconnected"
    invoke-virtual {v7, v4, v5}, Landroid/os/BaseBundle;->getBoolean(Ljava/lang/String;Z)Z
    move-result v13
    sget-object v4, Lcom/google/android/gms/internal/play_billing/n4;->zzb:Lcom/google/android/gms/internal/play_billing/n4;
    invoke-virtual {v10, v4}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z
    move-result v4
    if-nez v4, :cond_112
    invoke-virtual {v10, v1}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :cond_118
    :cond_112
    move-wide/from16 v19, v15
    move-wide v14, v11
    move-wide/from16 v11, v19
    goto :goto_152
    :cond_118
    sget-object v0, Lcom/google/android/gms/internal/play_billing/n4;->zzd:Lcom/google/android/gms/internal/play_billing/n4;
    invoke-virtual {v10, v0}, Ljava/lang/Object;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-eqz v0, :cond_151
    iget v0, v8, Lg6/h;->a:I
    if-eqz v0, :cond_132
    move-wide v11, v15
    invoke-virtual/range {v6 .. v13}, Lg6/m0;->c(Landroid/os/Bundle;Lg6/h;ILcom/google/android/gms/internal/play_billing/n4;JZ)V
    iget-object v0, v3, Lg6/n0;->b:Landroidx/work/impl/o;
    invoke-static {}, Lcom/google/android/gms/internal/play_billing/t;->zzk()Lcom/google/android/gms/internal/play_billing/t;
    move-result-object v1
    invoke-virtual {v0, v8, v1}, Landroidx/work/impl/o;->c(Lg6/h;Ljava/util/List;)V
    return-void
    :cond_132
    move-wide v6, v15
    invoke-virtual {v3}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    const-string v0, "No valid alternative billing listener is registered."
    invoke-static {v14, v0}, Lcom/google/android/gms/internal/play_billing/s;->h(Ljava/lang/String;Ljava/lang/String;)V
    sget-object v0, Lcom/google/android/gms/internal/play_billing/k4;->zzbK:Lcom/google/android/gms/internal/play_billing/k4;
    sget-object v1, Lg6/e0;->f:Lg6/h;
    invoke-static {v0, v9, v1, v2, v10}, Lg6/c0;->b(Lcom/google/android/gms/internal/play_billing/k4;ILg6/h;Ljava/lang/String;Lcom/google/android/gms/internal/play_billing/n4;)Lcom/google/android/gms/internal/play_billing/f4;
    move-result-object v0
    iget-object v2, v3, Lg6/n0;->c:Landroidx/work/impl/model/c;
    invoke-virtual {v2, v0, v6, v7, v13}, Landroidx/work/impl/model/c;->N0(Lcom/google/android/gms/internal/play_billing/f4;JZ)V
    invoke-static {}, Lcom/google/android/gms/internal/play_billing/t;->zzk()Lcom/google/android/gms/internal/play_billing/t;
    move-result-object v0
    iget-object v2, v3, Lg6/n0;->b:Landroidx/work/impl/o;
    invoke-virtual {v2, v1, v0}, Landroidx/work/impl/o;->c(Lg6/h;Ljava/util/List;)V
    :cond_151
    return-void
    :goto_152
    iget-object v1, v3, Lg6/n0;->g:Lcom/google/android/gms/internal/play_billing/y;
    const-string v4, "INAPP_PURCHASE_DATA_LIST"
    invoke-virtual {v7, v4}, Landroid/os/Bundle;->getStringArrayList(Ljava/lang/String;)Ljava/util/ArrayList;
    move-result-object v4
    const-string v6, "INAPP_DATA_SIGNATURE_LIST"
    invoke-virtual {v7, v6}, Landroid/os/Bundle;->getStringArrayList(Ljava/lang/String;)Ljava/util/ArrayList;
    move-result-object v6
    move/from16 p1, v0
    new-instance v0, Ljava/util/ArrayList;
    invoke-direct {v0}, Ljava/util/ArrayList;-><init>()V
    const-string v2, "BillingHelper"
    if-eqz v4, :cond_16d
    if-nez v6, :cond_170
    :cond_16d
    move-wide/from16 v17, v14
    goto :goto_1b3
    :cond_170
    invoke-interface {v4}, Ljava/util/List;->size()I
    move-result v5
    move-wide/from16 v17, v14
    new-instance v14, Ljava/lang/StringBuilder;
    const-string v15, "Found purchase list of "
    invoke-direct {v14, v15}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
    invoke-virtual {v14, v5}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;
    const-string v5, " items"
    invoke-virtual {v14, v5}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;
    invoke-virtual {v14}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v5
    invoke-static {v2, v5}, Lcom/google/android/gms/internal/play_billing/s;->g(Ljava/lang/String;Ljava/lang/String;)V
    const/4 v5, 0x0
    :goto_18d
    invoke-interface {v4}, Ljava/util/List;->size()I
    move-result v2
    if-ge v5, v2, :cond_1b1
    invoke-interface {v6}, Ljava/util/List;->size()I
    move-result v2
    if-ge v5, v2, :cond_1b1
    invoke-interface {v4, v5}, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v2
    check-cast v2, Ljava/lang/String;
    invoke-interface {v6, v5}, Ljava/util/List;->get(I)Ljava/lang/Object;
    move-result-object v14
    check-cast v14, Ljava/lang/String;
    invoke-static {v2, v14, v1}, Lcom/google/android/gms/internal/play_billing/s;->j(Ljava/lang/String;Ljava/lang/String;Ljava/util/Set;)Lg6/n;
    move-result-object v2
    if-eqz v2, :cond_1ae
    invoke-virtual {v0, v2}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
    :cond_1ae
    add-int/lit8 v5, v5, 0x1
    goto :goto_18d
    :cond_1b1
    :goto_1b1
    move-object v2, v0
    goto :goto_1d0
    :goto_1b3
    const-string v4, "INAPP_PURCHASE_DATA"
    invoke-virtual {v7, v4}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v4
    const-string v5, "INAPP_DATA_SIGNATURE"
    invoke-virtual {v7, v5}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v5
    invoke-static {v4, v5, v1}, Lcom/google/android/gms/internal/play_billing/s;->j(Ljava/lang/String;Ljava/lang/String;Ljava/util/Set;)Lg6/n;
    move-result-object v1
    if-nez v1, :cond_1cc
    const-string v0, "Couldn\'t find single purchase data as well."
    invoke-static {v2, v0}, Lcom/google/android/gms/internal/play_billing/s;->g(Ljava/lang/String;Ljava/lang/String;)V
    const/4 v2, 0x0
    goto :goto_1d0
    :cond_1cc
    invoke-virtual {v0, v1}, Ljava/util/ArrayList;->add(Ljava/lang/Object;)Z
    goto :goto_1b1
    :goto_1d0
    iget v0, v8, Lg6/h;->a:I
    if-nez v0, :cond_22f
    invoke-static {v9, v10}, Lg6/c0;->c(ILcom/google/android/gms/internal/play_billing/n4;)Lcom/google/android/gms/internal/play_billing/i4;
    move-result-object v0
    iget-object v1, v3, Lg6/n0;->c:Landroidx/work/impl/model/c;
    invoke-virtual {v1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    :try_start_1dd
    invoke-virtual {v0}, Lcom/google/android/gms/internal/play_billing/n2;->l()Lcom/google/android/gms/internal/play_billing/m2;
    move-result-object v4
    check-cast v4, Lcom/google/android/gms/internal/play_billing/h4;
    invoke-virtual {v0}, Lcom/google/android/gms/internal/play_billing/i4;->t()Lcom/google/android/gms/internal/play_billing/w4;
    move-result-object v0
    invoke-virtual {v0}, Lcom/google/android/gms/internal/play_billing/n2;->l()Lcom/google/android/gms/internal/play_billing/m2;
    move-result-object v0
    check-cast v0, Lcom/google/android/gms/internal/play_billing/u4;
    invoke-virtual {v0, v13}, Lcom/google/android/gms/internal/play_billing/u4;->g(Z)V
    invoke-virtual {v4}, Lcom/google/android/gms/internal/play_billing/m2;->d()V
    iget-object v5, v4, Lcom/google/android/gms/internal/play_billing/m2;->b:Lcom/google/android/gms/internal/play_billing/n2;
    check-cast v5, Lcom/google/android/gms/internal/play_billing/i4;
    invoke-virtual {v0}, Lcom/google/android/gms/internal/play_billing/m2;->b()Lcom/google/android/gms/internal/play_billing/n2;
    move-result-object v0
    check-cast v0, Lcom/google/android/gms/internal/play_billing/w4;
    invoke-static {v5, v0}, Lcom/google/android/gms/internal/play_billing/i4;->v(Lcom/google/android/gms/internal/play_billing/i4;Lcom/google/android/gms/internal/play_billing/w4;)V
    invoke-virtual {v4}, Lcom/google/android/gms/internal/play_billing/m2;->b()Lcom/google/android/gms/internal/play_billing/n2;
    move-result-object v0
    check-cast v0, Lcom/google/android/gms/internal/play_billing/i4;
    cmp-long v4, v11, v17
    if-nez v4, :cond_20f
    iget-object v4, v1, Landroidx/work/impl/model/c;->b:Ljava/lang/Object;
    check-cast v4, Lcom/google/android/gms/internal/play_billing/r4;
    goto :goto_222
    :cond_20f
    iget-object v4, v1, Landroidx/work/impl/model/c;->b:Ljava/lang/Object;
    check-cast v4, Lcom/google/android/gms/internal/play_billing/r4;
    invoke-virtual {v4}, Lcom/google/android/gms/internal/play_billing/n2;->l()Lcom/google/android/gms/internal/play_billing/m2;
    move-result-object v4
    check-cast v4, Lcom/google/android/gms/internal/play_billing/q4;
    invoke-virtual {v4, v11, v12}, Lcom/google/android/gms/internal/play_billing/q4;->i(J)V
    invoke-virtual {v4}, Lcom/google/android/gms/internal/play_billing/m2;->b()Lcom/google/android/gms/internal/play_billing/n2;
    move-result-object v4
    check-cast v4, Lcom/google/android/gms/internal/play_billing/r4;
    :goto_222
    invoke-virtual {v1, v0, v4}, Landroidx/work/impl/model/c;->V0(Lcom/google/android/gms/internal/play_billing/i4;Lcom/google/android/gms/internal/play_billing/r4;)V
    :try_end_225
    .catchall {:try_start_1dd .. :try_end_225} :catchall_226
    goto :goto_234
    :catchall_226
    move-exception v0
    const-string v1, "BillingLogger"
    const-string v4, "Unable to log."
    invoke-static {v1, v4, v0}, Lcom/google/android/gms/internal/play_billing/s;->i(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Throwable;)V
    goto :goto_234
    :cond_22f
    move-object/from16 v6, p0
    invoke-virtual/range {v6 .. v13}, Lg6/m0;->c(Landroid/os/Bundle;Lg6/h;ILcom/google/android/gms/internal/play_billing/n4;JZ)V
    :goto_234
    iget-object v0, v3, Lg6/n0;->b:Landroidx/work/impl/o;
    invoke-virtual {v0, v8, v2}, Landroidx/work/impl/o;->c(Lg6/h;Ljava/util/List;)V
    return-void
