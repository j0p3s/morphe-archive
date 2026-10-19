    invoke-super {p0}, Landroid/app/Service;->onCreate()V
    sget-object v0, Lcom/nieruo/healthapp/service/SleepService;->e:Ljava/util/concurrent/atomic/AtomicBoolean;
    const/4 v1, 0x1
    invoke-virtual {v0, v1}, Ljava/util/concurrent/atomic/AtomicBoolean;->set(Z)V
    sget-object v0, Lcom/nieruo/healthapp/service/SleepService;->f:Ljava/util/concurrent/atomic/AtomicBoolean;
    const/4 v1, 0x0
    invoke-virtual {v0, v1}, Ljava/util/concurrent/atomic/AtomicBoolean;->set(Z)V
    :try_start_f
    sget-object v0, Lkotlinx/datetime/w;->Companion:Lkotlinx/datetime/v;
    sget-object v2, Lcom/nieruo/healthapp/service/SleepService;->d:[Lxk/y;
    aget-object v1, v2, v1
    iget-object v2, p0, Lcom/nieruo/healthapp/service/SleepService;->a:Ldh/a;
    invoke-virtual {v2, p0, v1}, Ldh/a;->a(Ljava/lang/Object;Lxk/y;)Ljava/lang/Object;
    move-result-object v1
    check-cast v1, Lcom/nieruo/healthapp/entity/TempSleepEntity;
    if-eqz v1, :cond_26
    invoke-virtual {v1}, Lcom/nieruo/healthapp/entity/TempSleepEntity;->getEnd_dt()J
    move-result-wide v1
    goto :goto_4d
    :catchall_24
    move-exception v0
    goto :goto_82
    :cond_26
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    new-instance v1, Lkotlinx/datetime/w;
    invoke-static {}, Lj$/time/Clock;->systemUTC()Lj$/time/Clock;
    move-result-object v2
    invoke-virtual {v2}, Lj$/time/Clock;->instant()Lj$/time/Instant;
    move-result-object v2
    invoke-direct {v1, v2}, Lkotlinx/datetime/w;-><init>(Lj$/time/Instant;)V
    sget-object v2, Lkotlinx/datetime/i0;->Companion:Lkotlinx/datetime/h0;
    invoke-virtual {v2}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    invoke-static {}, Lkotlinx/datetime/h0;->a()Lkotlinx/datetime/i0;
    move-result-object v2
    invoke-static {v1, v2}, Landroidx/work/impl/model/f;->E(Lkotlinx/datetime/w;Lkotlinx/datetime/i0;)Lkotlinx/datetime/e0;
    move-result-object v1
    sget-object v2, Lkotlinx/datetime/i0;->b:Lkotlinx/datetime/t;
    invoke-static {v1, v2}, Landroidx/work/impl/model/f;->D(Lkotlinx/datetime/e0;Lkotlinx/datetime/i0;)Lkotlinx/datetime/w;
    move-result-object v1
    invoke-virtual {v1}, Lkotlinx/datetime/w;->c()J
    move-result-wide v1
    :goto_4d
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    invoke-static {v1, v2}, Lkotlinx/datetime/v;->a(J)Lkotlinx/datetime/w;
    move-result-object v0
    sget-object v1, Lkotlinx/datetime/i0;->Companion:Lkotlinx/datetime/h0;
    invoke-virtual {v1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    sget-object v1, Lkotlinx/datetime/i0;->b:Lkotlinx/datetime/t;
    invoke-static {v0, v1}, Landroidx/work/impl/model/f;->E(Lkotlinx/datetime/w;Lkotlinx/datetime/i0;)Lkotlinx/datetime/e0;
    move-result-object v0
    invoke-virtual {v0}, Lkotlinx/datetime/e0;->b()Lkotlinx/datetime/g0;
    move-result-object v0
    sget-object v1, Lcom/nieruo/healthapp/utils/x0;->a:Lcom/nieruo/healthapp/utils/x0;
    const-string v2, ""
    invoke-static {}, Lcom/nieruo/healthapp/utils/t;->P0()Lkotlinx/datetime/format/h1;
    move-result-object v3
    invoke-virtual {v3, v0}, Lkotlinx/datetime/format/a;->a(Ljava/lang/Object;)Ljava/lang/String;
    move-result-object v0
    invoke-virtual {v1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    invoke-static {p0, v2, v0}, Lcom/nieruo/healthapp/utils/x0;->h(Landroid/content/Context;Ljava/lang/String;Ljava/lang/String;)Landroid/app/Notification;
    move-result-object v0
    const/16 v1, 0x69
    invoke-virtual {p0, v1, v0}, Landroid/app/Service;->startForeground(ILandroid/app/Notification;)V
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    invoke-static {v0}, Lik/q;->constructor-impl(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    :try_end_81
    .catchall {:try_start_f .. :try_end_81} :catchall_24
    goto :goto_86
    :goto_82
    invoke-static {v0}, La0/a;->f(Ljava/lang/Throwable;)Ljava/lang/Object;
    move-result-object v0
    :goto_86
    invoke-static {v0}, Lik/q;->isFailure-impl(Ljava/lang/Object;)Z
    move-result v0
    const/4 v1, 0x0
    if-eqz v0, :cond_a6
    sget-object v0, Landroidx/lifecycle/x0;->i:Landroidx/lifecycle/x0;
    invoke-static {v0}, Landroidx/lifecycle/i1;->d(Landroidx/lifecycle/e0;)Landroidx/lifecycle/y;
    move-result-object v0
    new-instance v2, Lcom/nieruo/healthapp/service/c;
    const-wide/16 v3, 0x0
    invoke-direct {v2, v3, v4, p0, v1}, Lcom/nieruo/healthapp/service/c;-><init>(JLcom/nieruo/healthapp/service/SleepService;Lkotlin/coroutines/d;)V
    const/4 v3, 0x3
    invoke-static {v0, v1, v1, v2, v3}, Lkotlinx/coroutines/j0;->n(Lkotlinx/coroutines/f0;Lkotlin/coroutines/j;Lkotlinx/coroutines/h0;Lkotlin/jvm/functions/Function2;I)Lkotlinx/coroutines/e2;
    sget-object v0, Lcom/nieruo/healthapp/utils/x0;->a:Lcom/nieruo/healthapp/utils/x0;
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    invoke-static {p0}, Lcom/nieruo/healthapp/utils/x0;->g(Landroid/content/Context;)V
    :cond_a6
    new-instance v0, Landroid/content/IntentFilter;
    invoke-direct {v0}, Landroid/content/IntentFilter;-><init>()V
    const-string v2, "android.intent.action.TIME_SET"
    invoke-virtual {v0, v2}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V
    const-string v2, "android.intent.action.TIME_TICK"
    invoke-virtual {v0, v2}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V
    const/4 v2, 0x0
    const-string v2, "android.intent.action.DATE_CHANGED"
    invoke-virtual {v0, v2}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V
    const-string v2, "android.intent.action.TIMEZONE_CHANGED"
    invoke-virtual {v0, v2}, Landroid/content/IntentFilter;->addAction(Ljava/lang/String;)V
    iget-object v2, p0, Lcom/nieruo/healthapp/service/SleepService;->b:Lmi/a;
    const/4 v3, 0x4
    invoke-static {p0, v2, v0, v1, v3}, Lm0/a;->e(Landroid/content/Context;Landroid/content/BroadcastReceiver;Landroid/content/IntentFilter;Ljava/lang/String;I)Landroid/content/Intent;
    return-void
