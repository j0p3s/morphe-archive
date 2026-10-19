    if-eqz p1, :cond_ab
    const/4 v0, 0x0
    if-eqz p2, :cond_a
    invoke-virtual {p2}, Landroid/content/Intent;->getAction()Ljava/lang/String;
    move-result-object p2
    goto :goto_b
    :cond_a
    move-object p2, v0
    :goto_b
    const-string v1, "android.intent.action.MY_PACKAGE_REPLACED"
    invoke-static {p2, v1}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result p2
    if-nez p2, :cond_15
    goto/16 :goto_ab
    :cond_15
    new-instance p2, Landroidx/work/b0;
    const-class v1, Lcom/nieruo/healthapp/workers/AlarmReconcileWorker;
    invoke-direct {p2, v1}, Landroidx/work/p0;-><init>(Ljava/lang/Class;)V
    new-instance v1, Ljava/util/HashMap;
    invoke-direct {v1}, Ljava/util/HashMap;-><init>()V
    sget-object v2, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    const-string v3, "force_full_goal_rebuild"
    invoke-virtual {v1, v3, v2}, Ljava/util/HashMap;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    new-instance v2, Landroidx/work/j;
    invoke-direct {v2, v1}, Landroidx/work/j;-><init>(Ljava/util/HashMap;)V
    invoke-static {v2}, Landroidx/work/j;->c(Landroidx/work/j;)[B
    invoke-virtual {p2, v2}, Landroidx/work/p0;->g(Landroidx/work/j;)Landroidx/work/p0;
    move-result-object p2
    check-cast p2, Landroidx/work/b0;
    invoke-virtual {p2}, Landroidx/work/p0;->a()Landroidx/work/q0;
    move-result-object p2
    check-cast p2, Landroidx/work/c0;
    invoke-virtual {p1}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;
    move-result-object v1
    invoke-static {v1}, Landroidx/work/impl/t;->z(Landroid/content/Context;)Landroidx/work/impl/t;
    move-result-object v1
    const-string v2, "alarm_reconcile_after_upgrade"
    sget-object v3, Landroidx/work/m;->REPLACE:Landroidx/work/m;
    invoke-virtual {v1, v2, v3, p2}, Landroidx/work/o0;->e(Ljava/lang/String;Landroidx/work/m;Landroidx/work/c0;)Landroidx/work/g0;
    :try_start_4b
    new-instance p2, Lcom/nieruo/healthapp/workers/g;
    invoke-direct {p2, p1}, Lcom/nieruo/healthapp/workers/g;-><init>(Landroid/content/Context;)V
    sget-object p1, Lcom/nieruo/healthapp/realm/dao/g2;->e:Lcom/nieruo/healthapp/realm/dao/g;
    invoke-virtual {p1}, Lcom/nieruo/healthapp/realm/dao/g;->s()Lcom/nieruo/healthapp/realm/dao/g2;
    move-result-object p1
    const/4 v1, 0x1
    invoke-virtual {p1, v1}, Lcom/nieruo/healthapp/realm/dao/g2;->x(Z)Lcom/nieruo/healthapp/realm/entity/User;
    move-result-object p1
    if-eqz p1, :cond_a4
    invoke-virtual {p1}, Lcom/nieruo/healthapp/realm/entity/User;->z()Lgk/h;
    move-result-object p1
    if-eqz p1, :cond_a4
    invoke-static {p1}, Lcom/nieruo/healthapp/utils/t;->Q0(Lgk/h;)J
    move-result-wide v0
    sget-object p1, Lkotlinx/datetime/w;->Companion:Lkotlinx/datetime/v;
    invoke-virtual {p1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    new-instance p1, Lkotlinx/datetime/w;
    invoke-static {}, Lj$/time/Clock;->systemUTC()Lj$/time/Clock;
    move-result-object v2
    invoke-virtual {v2}, Lj$/time/Clock;->instant()Lj$/time/Instant;
    move-result-object v2
    invoke-direct {p1, v2}, Lkotlinx/datetime/w;-><init>(Lj$/time/Instant;)V
    sget-object v2, Lkotlinx/datetime/i0;->Companion:Lkotlinx/datetime/h0;
    invoke-virtual {v2}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    invoke-static {}, Lkotlinx/datetime/h0;->a()Lkotlinx/datetime/i0;
    move-result-object v2
    invoke-static {p1, v2}, Landroidx/work/impl/model/f;->E(Lkotlinx/datetime/w;Lkotlinx/datetime/i0;)Lkotlinx/datetime/e0;
    move-result-object p1
    sget-object v2, Lkotlinx/datetime/i0;->b:Lkotlinx/datetime/t;
    invoke-static {p1, v2}, Landroidx/work/impl/model/f;->D(Lkotlinx/datetime/e0;Lkotlinx/datetime/i0;)Lkotlinx/datetime/w;
    move-result-object p1
    invoke-virtual {p1}, Lkotlinx/datetime/w;->c()J
    move-result-wide v2
    sub-long/2addr v0, v2
    const-wide/16 v2, 0x0
    cmp-long p1, v0, v2
    if-lez p1, :cond_a2
    invoke-static {}, Ljava/lang/System;->currentTimeMillis()J
    move-result-wide v2
    add-long/2addr v2, v0
    invoke-virtual {p2, v2, v3}, Lcom/nieruo/healthapp/workers/g;->m(J)V
    goto :goto_a2
    :catchall_a0
    move-exception p1
    goto :goto_a8
    :cond_a2
    :goto_a2
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    :cond_a4
    invoke-static {v0}, Lik/q;->constructor-impl(Ljava/lang/Object;)Ljava/lang/Object;
    :try_end_a7
    .catchall {:try_start_4b .. :try_end_a7} :catchall_a0
    return-void
    :goto_a8
    invoke-static {p1}, La0/a;->C(Ljava/lang/Throwable;)V
    :cond_ab
    :goto_ab
    return-void
