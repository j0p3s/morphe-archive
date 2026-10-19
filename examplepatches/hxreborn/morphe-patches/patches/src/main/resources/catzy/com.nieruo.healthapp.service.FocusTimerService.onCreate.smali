    move-object/from16 v1, p0
    const-string v2, ""
    invoke-super {v1}, Landroid/app/Service;->onCreate()V
    sget-object v0, Lcom/nieruo/healthapp/service/FocusTimerService;->h:Ljava/util/concurrent/atomic/AtomicBoolean;
    const/4 v3, 0x1
    invoke-virtual {v0, v3}, Ljava/util/concurrent/atomic/AtomicBoolean;->set(Z)V
    sget-object v0, Lcom/nieruo/healthapp/service/FocusTimerService;->i:Ljava/util/concurrent/atomic/AtomicBoolean;
    const/4 v4, 0x0
    invoke-virtual {v0, v4}, Ljava/util/concurrent/atomic/AtomicBoolean;->set(Z)V
    new-instance v0, Ll0/a0;
    invoke-direct {v0, v1}, Ll0/a0;-><init>(Landroid/content/Context;)V
    sget v5, Landroid/os/Build$VERSION;->SDK_INT:I
    const/16 v6, 0x1a
    if-lt v5, v6, :cond_3f
    const/4 v7, 0x0
    const-string v8, "focus_timer_channel"
    if-lt v5, v6, :cond_2a
    iget-object v5, v0, Ll0/a0;->b:Landroid/app/NotificationManager;
    invoke-static {v5, v8}, Landroidx/core/view/d1;->b(Landroid/app/NotificationManager;Ljava/lang/String;)Landroid/app/NotificationChannel;
    move-result-object v5
    goto :goto_2b
    :cond_2a
    move-object v5, v7
    :goto_2b
    if-nez v5, :cond_3f
    new-instance v5, Lio/realm/kotlin/internal/y0;
    invoke-direct {v5, v8}, Lio/realm/kotlin/internal/y0;-><init>(Ljava/lang/String;)V
    iput-object v8, v5, Lio/realm/kotlin/internal/y0;->e:Ljava/lang/Object;
    iput-object v7, v5, Lio/realm/kotlin/internal/y0;->f:Ljava/lang/Object;
    iput-object v7, v5, Lio/realm/kotlin/internal/y0;->g:Ljava/lang/Object;
    iput-boolean v4, v5, Lio/realm/kotlin/internal/y0;->c:Z
    sget-object v4, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    invoke-virtual {v0, v5}, Ll0/a0;->b(Lio/realm/kotlin/internal/y0;)V
    :cond_3f
    invoke-virtual {v1}, Lcom/nieruo/healthapp/service/FocusTimerService;->a()Lcom/nieruo/healthapp/entity/TempFocusEntity;
    move-result-object v0
    const-string v4, "service_first_startforeground_attempt"
    invoke-static {v4}, Lu4/h;->R(Ljava/lang/String;)V
    invoke-static {v0}, Lu4/h;->O(Lcom/nieruo/healthapp/entity/TempFocusEntity;)V
    invoke-static {v1}, Lu4/h;->S(Lcom/nieruo/healthapp/service/FocusTimerService;)V
    sget-wide v4, Lu4/h;->c:J
    const-wide/16 v6, 0x0
    cmp-long v0, v4, v6
    if-gtz v0, :cond_59
    const-wide/16 v4, -0x1
    goto :goto_60
    :cond_59
    invoke-static {}, Landroid/os/SystemClock;->elapsedRealtime()J
    move-result-wide v4
    sget-wide v8, Lu4/h;->c:J
    sub-long/2addr v4, v8
    :goto_60
    const-string v0, "startforeground_attempt_elapsed"
    invoke-static {v4, v5, v0}, Lu4/h;->Q(JLjava/lang/String;)V
    const-string v0, "first_startForeground_attempt"
    invoke-static {v0}, Lu4/h;->z(Ljava/lang/String;)V
    const/16 v4, 0x68
    :try_start_6c
    invoke-virtual {v1}, Lcom/nieruo/healthapp/service/FocusTimerService;->a()Lcom/nieruo/healthapp/entity/TempFocusEntity;
    move-result-object v0
    if-eqz v0, :cond_7b
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getTag()Ljava/lang/String;
    move-result-object v0
    if-nez v0, :cond_7c
    goto :goto_7b
    :catchall_79
    move-exception v0
    goto :goto_8a
    :cond_7b
    :goto_7b
    move-object v0, v2
    :cond_7c
    invoke-virtual {v1, v0, v2}, Lcom/nieruo/healthapp/service/FocusTimerService;->d(Ljava/lang/String;Ljava/lang/String;)Landroid/app/Notification;
    move-result-object v0
    invoke-virtual {v1, v4, v0}, Landroid/app/Service;->startForeground(ILandroid/app/Notification;)V
    sget-object v0, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    invoke-static {v0}, Lik/q;->constructor-impl(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    :try_end_89
    .catchall {:try_start_6c .. :try_end_89} :catchall_79
    goto :goto_8e
    :goto_8a
    invoke-static {v0}, La0/a;->f(Ljava/lang/Throwable;)Ljava/lang/Object;
    move-result-object v0
    :goto_8e
    invoke-static {v0}, Lik/q;->isSuccess-impl(Ljava/lang/Object;)Z
    move-result v5
    const-wide/16 v8, 0x7d0
    const-string v10, "startforeground_result"
    if-eqz v5, :cond_218
    invoke-virtual {v1}, Lcom/nieruo/healthapp/service/FocusTimerService;->a()Lcom/nieruo/healthapp/entity/TempFocusEntity;
    move-result-object v0
    const-string v5, "service_first_startforeground_success"
    invoke-static {v5}, Lu4/h;->R(Ljava/lang/String;)V
    invoke-static {v0}, Lu4/h;->O(Lcom/nieruo/healthapp/entity/TempFocusEntity;)V
    invoke-static {v1}, Lu4/h;->S(Lcom/nieruo/healthapp/service/FocusTimerService;)V
    const-string v0, "success"
    invoke-static {v10, v0}, Lu4/h;->T(Ljava/lang/String;Ljava/lang/String;)V
    const-string v0, "first_startForeground_success"
    invoke-static {v0}, Lu4/h;->z(Ljava/lang/String;)V
    invoke-virtual {v1}, Lcom/nieruo/healthapp/service/FocusTimerService;->a()Lcom/nieruo/healthapp/entity/TempFocusEntity;
    move-result-object v0
    if-eqz v0, :cond_214
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getTag()Ljava/lang/String;
    move-result-object v5
    sget-object v8, Lkotlinx/datetime/w;->Companion:Lkotlinx/datetime/v;
    invoke-virtual {v8}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    new-instance v8, Lkotlinx/datetime/w;
    invoke-static {}, Lj$/time/Clock;->systemUTC()Lj$/time/Clock;
    move-result-object v9
    invoke-virtual {v9}, Lj$/time/Clock;->instant()Lj$/time/Instant;
    move-result-object v9
    invoke-direct {v8, v9}, Lkotlinx/datetime/w;-><init>(Lj$/time/Instant;)V
    sget-object v9, Lkotlinx/datetime/i0;->Companion:Lkotlinx/datetime/h0;
    invoke-static {v9, v8}, Lcom/nieruo/healthapp/d;->q(Lkotlinx/datetime/h0;Lkotlinx/datetime/w;)Lkotlinx/datetime/e0;
    move-result-object v8
    sget-object v9, Lkotlinx/datetime/i0;->b:Lkotlinx/datetime/t;
    invoke-static {v8, v9}, Landroidx/work/impl/model/f;->D(Lkotlinx/datetime/e0;Lkotlinx/datetime/i0;)Lkotlinx/datetime/w;
    move-result-object v8
    iget-object v8, v8, Lkotlinx/datetime/w;->a:Lj$/time/Instant;
    invoke-virtual {v8}, Lj$/time/Instant;->getEpochSecond()J
    move-result-wide v8
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getType()I
    move-result v10
    const-string v11, "%d:%02d"
    const/16 v12, 0x3c
    const/16 v13, 0xe10
    const/4 v14, 0x2
    if-eqz v10, :cond_1bc
    if-eq v10, v3, :cond_170
    if-eq v10, v14, :cond_f2
    goto/16 :goto_209
    :cond_f2
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getPomodoro()Lcom/nieruo/healthapp/entity/PomodoroState;
    move-result-object v3
    if-nez v3, :cond_fa
    goto/16 :goto_209
    :cond_fa
    invoke-virtual {v3}, Lcom/nieruo/healthapp/entity/PomodoroState;->getPhase()Ljava/lang/String;
    move-result-object v2
    const-string v10, "focus"
    invoke-static {v2, v10}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :cond_10b
    invoke-virtual {v3}, Lcom/nieruo/healthapp/entity/PomodoroState;->getFocusMinutes()I
    move-result v2
    goto :goto_120
    :cond_10b
    invoke-virtual {v3}, Lcom/nieruo/healthapp/entity/PomodoroState;->getPhase()Ljava/lang/String;
    move-result-object v2
    const-string v10, "long_rest"
    invoke-static {v2, v10}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :cond_11c
    invoke-virtual {v3}, Lcom/nieruo/healthapp/entity/PomodoroState;->getLongRestMinutes()I
    move-result v2
    goto :goto_120
    :cond_11c
    invoke-virtual {v3}, Lcom/nieruo/healthapp/entity/PomodoroState;->getShortRestMinutes()I
    move-result v2
    :goto_120
    int-to-long v2, v2
    const-wide/16 v15, 0x3c
    mul-long/2addr v2, v15
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getPause_dt()Ljava/lang/Long;
    move-result-object v10
    if-eqz v10, :cond_12e
    invoke-virtual {v10}, Ljava/lang/Long;->longValue()J
    move-result-wide v8
    :cond_12e
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getStart_dt()J
    move-result-wide v15
    sub-long/2addr v8, v15
    cmp-long v0, v8, v6
    if-gez v0, :cond_138
    move-wide v8, v6
    :cond_138
    cmp-long v0, v8, v2
    if-lez v0, :cond_13d
    goto :goto_13e
    :cond_13d
    move-wide v2, v8
    :goto_13e
    int-to-long v8, v13
    div-long v8, v2, v8
    cmp-long v0, v8, v6
    if-gez v0, :cond_146
    move-wide v8, v6
    :cond_146
    int-to-long v12, v12
    div-long v15, v2, v12
    rem-long/2addr v15, v12
    cmp-long v0, v15, v6
    if-gez v0, :cond_14f
    move-wide v15, v6
    :cond_14f
    mul-long/2addr v8, v12
    add-long/2addr v8, v15
    rem-long/2addr v2, v12
    cmp-long v0, v2, v6
    if-gez v0, :cond_157
    goto :goto_158
    :cond_157
    move-wide v6, v2
    :goto_158
    sget-object v0, Lkotlin/jvm/internal/StringCompanionObject;->INSTANCE:Lkotlin/jvm/internal/StringCompanionObject;
    invoke-static {v8, v9}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v0
    invoke-static {v6, v7}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v2
    filled-new-array {v0, v2}, [Ljava/lang/Object;
    move-result-object v0
    invoke-static {v0, v14}, Ljava/util/Arrays;->copyOf([Ljava/lang/Object;I)[Ljava/lang/Object;
    move-result-object v0
    invoke-static {v11, v0}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    move-result-object v2
    goto/16 :goto_209
    :cond_170
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getPause_dt()Ljava/lang/Long;
    move-result-object v2
    if-eqz v2, :cond_186
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getPause_dt()Ljava/lang/Long;
    move-result-object v2
    if-eqz v2, :cond_180
    invoke-virtual {v2}, Ljava/lang/Long;->longValue()J
    move-result-wide v8
    :cond_180
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getStart_dt()J
    move-result-wide v2
    :goto_184
    sub-long/2addr v8, v2
    goto :goto_18b
    :cond_186
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getStart_dt()J
    move-result-wide v2
    goto :goto_184
    :goto_18b
    int-to-long v2, v13
    div-long v2, v8, v2
    cmp-long v0, v2, v6
    if-gez v0, :cond_193
    move-wide v2, v6
    :cond_193
    int-to-long v12, v12
    div-long v15, v8, v12
    rem-long/2addr v15, v12
    cmp-long v0, v15, v6
    if-gez v0, :cond_19c
    move-wide v15, v6
    :cond_19c
    mul-long/2addr v2, v12
    add-long/2addr v2, v15
    rem-long/2addr v8, v12
    cmp-long v0, v8, v6
    if-gez v0, :cond_1a4
    goto :goto_1a5
    :cond_1a4
    move-wide v6, v8
    :goto_1a5
    sget-object v0, Lkotlin/jvm/internal/StringCompanionObject;->INSTANCE:Lkotlin/jvm/internal/StringCompanionObject;
    invoke-static {v2, v3}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v0
    invoke-static {v6, v7}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v2
    filled-new-array {v0, v2}, [Ljava/lang/Object;
    move-result-object v0
    invoke-static {v0, v14}, Ljava/util/Arrays;->copyOf([Ljava/lang/Object;I)[Ljava/lang/Object;
    move-result-object v0
    invoke-static {v11, v0}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    move-result-object v2
    goto :goto_209
    :cond_1bc
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getPause_dt()Ljava/lang/Long;
    move-result-object v2
    if-eqz v2, :cond_1d4
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getEnd_dt()J
    move-result-wide v2
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getPause_dt()Ljava/lang/Long;
    move-result-object v0
    if-eqz v0, :cond_1d1
    invoke-virtual {v0}, Ljava/lang/Long;->longValue()J
    move-result-wide v8
    goto :goto_1d2
    :cond_1d1
    move-wide v8, v6
    :goto_1d2
    sub-long/2addr v2, v8
    goto :goto_1d9
    :cond_1d4
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/TempFocusEntity;->getEnd_dt()J
    move-result-wide v2
    goto :goto_1d2
    :goto_1d9
    int-to-long v8, v13
    div-long v8, v2, v8
    cmp-long v0, v8, v6
    if-gez v0, :cond_1e1
    move-wide v8, v6
    :cond_1e1
    int-to-long v12, v12
    div-long v15, v2, v12
    rem-long/2addr v15, v12
    cmp-long v0, v15, v6
    if-gez v0, :cond_1ea
    move-wide v15, v6
    :cond_1ea
    mul-long/2addr v8, v12
    add-long/2addr v8, v15
    rem-long/2addr v2, v12
    cmp-long v0, v2, v6
    if-gez v0, :cond_1f2
    goto :goto_1f3
    :cond_1f2
    move-wide v6, v2
    :goto_1f3
    sget-object v0, Lkotlin/jvm/internal/StringCompanionObject;->INSTANCE:Lkotlin/jvm/internal/StringCompanionObject;
    invoke-static {v8, v9}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v0
    invoke-static {v6, v7}, Ljava/lang/Long;->valueOf(J)Ljava/lang/Long;
    move-result-object v2
    filled-new-array {v0, v2}, [Ljava/lang/Object;
    move-result-object v0
    invoke-static {v0, v14}, Ljava/util/Arrays;->copyOf([Ljava/lang/Object;I)[Ljava/lang/Object;
    move-result-object v0
    invoke-static {v11, v0}, Ljava/lang/String;->format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;
    move-result-object v2
    :goto_209
    invoke-virtual {v1, v5, v2}, Lcom/nieruo/healthapp/service/FocusTimerService;->d(Ljava/lang/String;Ljava/lang/String;)Landroid/app/Notification;
    move-result-object v0
    invoke-virtual {v1, v4, v0}, Landroid/app/Service;->startForeground(ILandroid/app/Notification;)V
    invoke-virtual {v1}, Lcom/nieruo/healthapp/service/FocusTimerService;->c()V
    goto :goto_267
    :cond_214
    invoke-virtual {v1, v8, v9}, Lcom/nieruo/healthapp/service/FocusTimerService;->e(J)V
    goto :goto_267
    :cond_218
    invoke-static {v0}, Lik/q;->exceptionOrNull-impl(Ljava/lang/Object;)Ljava/lang/Throwable;
    move-result-object v0
    if-eqz v0, :cond_264
    invoke-virtual {v1}, Lcom/nieruo/healthapp/service/FocusTimerService;->a()Lcom/nieruo/healthapp/entity/TempFocusEntity;
    move-result-object v2
    const-string v3, "service_first_startforeground_failed"
    invoke-static {v3}, Lu4/h;->R(Ljava/lang/String;)V
    invoke-static {v2}, Lu4/h;->O(Lcom/nieruo/healthapp/entity/TempFocusEntity;)V
    invoke-static {v1}, Lu4/h;->S(Lcom/nieruo/healthapp/service/FocusTimerService;)V
    const-string v2, "failed"
    invoke-static {v10, v2}, Lu4/h;->T(Ljava/lang/String;Ljava/lang/String;)V
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    move-result-object v2
    invoke-virtual {v2}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;
    move-result-object v2
    const-string v3, "startforeground_exception"
    invoke-static {v3, v2}, Lu4/h;->T(Ljava/lang/String;Ljava/lang/String;)V
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    move-result-object v2
    invoke-virtual {v2}, Ljava/lang/Class;->getSimpleName()Ljava/lang/String;
    move-result-object v2
    const-string v3, "first_startForeground_failed "
    invoke-virtual {v3, v2}, Ljava/lang/String;->concat(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v2
    invoke-static {v2}, Lu4/h;->z(Ljava/lang/String;)V
    invoke-static {}, Ljd/c;->a()Ljd/c;
    move-result-object v2
    sget-object v3, Ljava/util/Collections;->EMPTY_MAP:Ljava/util/Map;
    iget-object v2, v2, Ljd/c;->a:Lnd/r;
    iget-object v3, v2, Lnd/r;->o:Lod/j;
    iget-object v3, v3, Lod/j;->a:Lod/b;
    new-instance v4, Lcom/nieruo/healthapp/ui/fragment/h3;
    invoke-direct {v4, v2, v0}, Lcom/nieruo/healthapp/ui/fragment/h3;-><init>(Lnd/r;Ljava/lang/Throwable;)V
    invoke-virtual {v3, v4}, Lod/b;->a(Ljava/lang/Runnable;)Lhb/q;
    :cond_264
    invoke-virtual {v1, v8, v9}, Lcom/nieruo/healthapp/service/FocusTimerService;->e(J)V
    :goto_267
    return-void
