    move-object/from16 v4, p1
    if-nez v4, :cond_6
    goto/16 :goto_28f
    :cond_6
    new-instance v0, Ll0/a0;
    invoke-direct {v0, v4}, Ll0/a0;-><init>(Landroid/content/Context;)V
    if-eqz p2, :cond_1e
    invoke-virtual/range {p2 .. p2}, Landroid/content/Intent;->getExtras()Landroid/os/Bundle;
    move-result-object v1
    if-eqz v1, :cond_1e
    const-string v2, "key_notification_channel"
    invoke-virtual {v1, v2}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v1
    if-nez v1, :cond_1c
    goto :goto_1e
    :cond_1c
    :goto_1c
    move-object v6, v1
    goto :goto_21
    :cond_1e
    :goto_1e
    const-string v1, "scheduled_goal_channel"
    goto :goto_1c
    :goto_21
    if-eqz p2, :cond_31
    invoke-virtual/range {p2 .. p2}, Landroid/content/Intent;->getExtras()Landroid/os/Bundle;
    move-result-object v1
    if-eqz v1, :cond_31
    const-string v2, "key_notification_type"
    invoke-virtual {v1, v2}, Landroid/os/BaseBundle;->getInt(Ljava/lang/String;)I
    move-result v1
    move v8, v1
    goto :goto_32
    :cond_31
    const/4 v8, 0x0
    :goto_32
    sget v1, Landroid/os/Build$VERSION;->SDK_INT:I
    const/16 v2, 0x1a
    if-lt v1, v2, :cond_5a
    new-instance v1, Lio/realm/kotlin/internal/y0;
    invoke-direct {v1, v6}, Lio/realm/kotlin/internal/y0;-><init>(Ljava/lang/String;)V
    iput-object v6, v1, Lio/realm/kotlin/internal/y0;->e:Ljava/lang/Object;
    invoke-virtual/range {p0 .. p0}, Lcom/nieruo/healthapp/receiver/ScheduledNotificationReceiver;->a()Ljava/lang/Boolean;
    move-result-object v2
    sget-object v3, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    invoke-static {v2, v3}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    iput-boolean v2, v1, Lio/realm/kotlin/internal/y0;->c:Z
    invoke-virtual/range {p0 .. p0}, Lcom/nieruo/healthapp/receiver/ScheduledNotificationReceiver;->a()Ljava/lang/Boolean;
    move-result-object v2
    invoke-static {v2, v3}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    iput-boolean v2, v1, Lio/realm/kotlin/internal/y0;->b:Z
    sget-object v2, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    invoke-virtual {v0, v1}, Ll0/a0;->b(Lio/realm/kotlin/internal/y0;)V
    :cond_5a
    invoke-static {v4}, Lz7/c;->c(Landroid/content/Context;)Z
    move-result v0
    if-eqz v0, :cond_28f
    sget-object v0, Lcom/nieruo/healthapp/manager/a1;->m:Lfe/d;
    invoke-virtual {v0}, Lfe/d;->t()Lcom/nieruo/healthapp/manager/a1;
    move-result-object v0
    invoke-virtual {v0}, Lcom/nieruo/healthapp/manager/a1;->z()Z
    move-result v0
    if-nez v0, :cond_6e
    goto/16 :goto_28f
    :cond_6e
    const-string v0, ""
    if-eqz p2, :cond_83
    invoke-virtual/range {p2 .. p2}, Landroid/content/Intent;->getExtras()Landroid/os/Bundle;
    move-result-object v1
    if-eqz v1, :cond_83
    const-string v2, "key_goal_notification_title"
    invoke-virtual {v1, v2}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v1
    if-nez v1, :cond_81
    goto :goto_83
    :cond_81
    move-object v2, v1
    goto :goto_84
    :cond_83
    :goto_83
    move-object v2, v0
    :goto_84
    const-string v1, "key_goal_notification_content"
    if-eqz p2, :cond_94
    invoke-virtual/range {p2 .. p2}, Landroid/content/Intent;->getExtras()Landroid/os/Bundle;
    move-result-object v3
    if-eqz v3, :cond_94
    invoke-virtual {v3, v1}, Landroid/os/BaseBundle;->getString(Ljava/lang/String;)Ljava/lang/String;
    move-result-object v3
    if-nez v3, :cond_95
    :cond_94
    move-object v3, v0
    :cond_95
    if-eqz p2, :cond_a5
    invoke-virtual/range {p2 .. p2}, Landroid/content/Intent;->getExtras()Landroid/os/Bundle;
    move-result-object v0
    if-eqz v0, :cond_a5
    const-string v5, "key_goal_notification_id"
    invoke-virtual {v0, v5}, Landroid/os/BaseBundle;->getInt(Ljava/lang/String;)I
    move-result v0
    :goto_a3
    move v9, v0
    goto :goto_a7
    :cond_a5
    const/4 v0, -0x1
    goto :goto_a3
    :goto_a7
    const-class v11, Lcom/nieruo/healthapp/realm/entity/Goal;
    const-string v12, "primary_key == $0"
    const/4 v13, 0x0
    const/4 v14, 0x2
    sget-object v15, Lcom/nieruo/healthapp/receiver/ScheduledNotificationReceiver;->b:[Lxk/y;
    const-string v5, "goal_schedule_entity"
    const/16 v16, 0x0
    const/4 v7, 0x1
    if-nez v8, :cond_191
    invoke-static {}, Lcom/nieruo/healthapp/utils/t;->s()Z
    move-result v0
    if-nez v0, :cond_bf
    :goto_bc
    move v0, v7
    goto/16 :goto_18f
    :cond_bf
    :try_start_bf
    aget-object v0, v15, v14
    sget-object v0, Lcom/orhanobut/hawk/c;->a:Lcom/orhanobut/hawk/h;
    invoke-interface {v0, v13, v5}, Lcom/orhanobut/hawk/h;->n(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/entity/ScheduleEntity;
    if-eqz v0, :cond_188
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/ScheduleEntity;->getScheduleList()Ljava/util/List;
    move-result-object v0
    if-eqz v0, :cond_188
    invoke-interface {v0}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
    move-result-object v0
    :goto_d5
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z
    move-result v17
    if-eqz v17, :cond_ef
    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v17
    move-object/from16 v18, v17
    check-cast v18, Lcom/nieruo/healthapp/entity/ScheduleItem;
    invoke-virtual/range {v18 .. v18}, Lcom/nieruo/healthapp/entity/ScheduleItem;->getRequestCode()I
    move-result v13
    if-ne v13, v9, :cond_ea
    goto :goto_f1
    :cond_ea
    const/4 v13, 0x0
    goto :goto_d5
    :catchall_ec
    move-exception v0
    goto/16 :goto_18b
    :cond_ef
    const/16 v17, 0x0
    :goto_f1
    check-cast v17, Lcom/nieruo/healthapp/entity/ScheduleItem;
    if-eqz v17, :cond_188
    invoke-static {}, Lcom/nieruo/healthapp/realm/g;->a()Lsj/e;
    move-result-object v0
    invoke-virtual/range {v17 .. v17}, Lcom/nieruo/healthapp/entity/ScheduleItem;->getPrimaryKey()Lmo/i0;
    move-result-object v13
    filled-new-array {v13}, [Ljava/lang/Object;
    move-result-object v13
    invoke-static {v11}, Lkotlin/jvm/internal/Reflection;->getOrCreateKotlinClass(Ljava/lang/Class;)Lxk/d;
    move-result-object v10
    invoke-static {v13, v7}, Ljava/util/Arrays;->copyOf([Ljava/lang/Object;I)[Ljava/lang/Object;
    move-result-object v13
    check-cast v0, Lio/realm/kotlin/internal/g1;
    invoke-virtual {v0, v10, v12, v13}, Lio/realm/kotlin/internal/g1;->i(Lxk/d;Ljava/lang/String;[Ljava/lang/Object;)Lio/realm/kotlin/internal/query/c;
    move-result-object v0
    invoke-virtual {v0}, Lio/realm/kotlin/internal/query/c;->first()Ldk/d;
    move-result-object v0
    check-cast v0, Lio/realm/kotlin/internal/query/j;
    invoke-virtual {v0}, Lio/realm/kotlin/internal/query/j;->a()Lgk/a;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/realm/entity/Goal;
    if-eqz v0, :cond_188
    invoke-virtual {v0}, Lcom/nieruo/healthapp/realm/entity/Goal;->o()Lgk/i;
    move-result-object v10
    const-string v13, "dt == $0"
    invoke-static/range {v16 .. v16}, Lcom/nieruo/healthapp/utils/a1;->i(I)Lgk/h;
    move-result-object v18
    filled-new-array/range {v18 .. v18}, [Ljava/lang/Object;
    move-result-object v14
    invoke-static {v10, v13, v14}, Lp1/a;->x(Lgk/i;Ljava/lang/String;[Ljava/lang/Object;)Landroidx/work/impl/model/e;
    move-result-object v10
    invoke-virtual {v10}, Landroidx/work/impl/model/e;->first()Ldk/d;
    move-result-object v10
    check-cast v10, Landroidx/work/impl/model/m;
    iget-object v10, v10, Landroidx/work/impl/model/m;->c:Ljava/lang/Object;
    check-cast v10, Ldk/d;
    invoke-interface {v10}, Ldk/d;->a()Lgk/a;
    move-result-object v10
    check-cast v10, Lcom/nieruo/healthapp/realm/entity/GoalTask;
    invoke-virtual {v0}, Lcom/nieruo/healthapp/realm/entity/Goal;->K()Z
    move-result v13
    if-nez v13, :cond_183
    invoke-virtual {v0}, Lcom/nieruo/healthapp/realm/entity/Goal;->q()I
    move-result v13
    if-eq v13, v7, :cond_183
    invoke-virtual {v0}, Lcom/nieruo/healthapp/realm/entity/Goal;->q()I
    move-result v13
    const/4 v14, 0x2
    if-ne v13, v14, :cond_153
    goto :goto_183
    :cond_153
    invoke-virtual {v0}, Lcom/nieruo/healthapp/realm/entity/Goal;->P()Z
    move-result v13
    if-eqz v13, :cond_171
    invoke-virtual {v0}, Lcom/nieruo/healthapp/realm/entity/Goal;->o()Lgk/i;
    move-result-object v0
    invoke-interface {v0}, Ljava/util/Collection;->isEmpty()Z
    move-result v10
    if-nez v10, :cond_16e
    invoke-static {v0}, Lkotlin/collections/b0;->p0(Ljava/util/List;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/realm/entity/GoalTask;
    invoke-virtual {v0}, Lcom/nieruo/healthapp/realm/entity/GoalTask;->x()Z
    move-result v0
    goto :goto_184
    :cond_16e
    move/from16 v0, v16
    goto :goto_184
    :cond_171
    if-eqz v10, :cond_17a
    invoke-virtual {v10}, Lcom/nieruo/healthapp/realm/entity/GoalTask;->x()Z
    move-result v0
    if-ne v0, v7, :cond_17a
    goto :goto_183
    :cond_17a
    if-eqz v10, :cond_16e
    invoke-virtual {v10}, Lcom/nieruo/healthapp/realm/entity/GoalTask;->k()I
    move-result v0
    :try_end_180
    .catchall {:try_start_bf .. :try_end_180} :catchall_ec
    const/4 v10, 0x3
    if-ne v0, v10, :cond_16e
    :cond_183
    :goto_183
    move v0, v7
    :goto_184
    if-eqz v0, :cond_188
    goto/16 :goto_bc
    :cond_188
    :goto_188
    move/from16 v0, v16
    goto :goto_18f
    :goto_18b
    invoke-virtual {v0}, Ljava/lang/Throwable;->getMessage()Ljava/lang/String;
    goto :goto_188
    :goto_18f
    xor-int/2addr v0, v7
    goto :goto_192
    :cond_191
    move v0, v7
    :goto_192
    if-eqz v0, :cond_1f1
    new-instance v0, Landroid/content/Intent;
    const-class v10, Lcom/nieruo/healthapp/ui/activity/HomeActivity;
    invoke-direct {v0, v4, v10}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
    const/high16 v10, 0x10000000
    invoke-virtual {v0, v10}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;
    new-instance v10, Landroid/os/Bundle;
    invoke-direct {v10}, Landroid/os/Bundle;-><init>()V
    const-string v13, "type"
    invoke-static {v8}, Ljava/lang/String;->valueOf(I)Ljava/lang/String;
    move-result-object v14
    invoke-virtual {v10, v13, v14}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V
    invoke-virtual {v3}, Ljava/lang/String;->length()I
    move-result v13
    if-nez v13, :cond_1b6
    move v13, v7
    goto :goto_1b8
    :cond_1b6
    move/from16 v13, v16
    :goto_1b8
    if-eqz v13, :cond_1bc
    move-object v13, v2
    goto :goto_1bd
    :cond_1bc
    move-object v13, v3
    :goto_1bd
    invoke-virtual {v10, v1, v13}, Landroid/os/BaseBundle;->putString(Ljava/lang/String;Ljava/lang/String;)V
    invoke-virtual {v0, v10}, Landroid/content/Intent;->putExtras(Landroid/os/Bundle;)Landroid/content/Intent;
    sget-object v1, Lkotlin/Unit;->INSTANCE:Lkotlin/Unit;
    const/high16 v1, 0xc000000
    invoke-static {}, Lcom/nieruo/healthapp/utils/t;->N0()Landroid/os/Bundle;
    move-result-object v10
    invoke-static {v4, v9, v0, v1, v10}, Landroid/app/PendingIntent;->getActivity(Landroid/content/Context;ILandroid/content/Intent;ILandroid/os/Bundle;)Landroid/app/PendingIntent;
    move-result-object v0
    move-object v1, v5
    move-object v5, v0
    new-instance v0, Lcom/nieruo/healthapp/realm/dao/w0;
    move-object v10, v1
    const/16 v1, 0x12
    invoke-direct/range {v0 .. v5}, Lcom/nieruo/healthapp/realm/dao/w0;-><init>(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V
    invoke-static {v4}, Lz7/c;->c(Landroid/content/Context;)Z
    move-result v1
    if-nez v1, :cond_1e0
    goto :goto_1f2
    :cond_1e0
    invoke-static {v4, v6, v0}, Lz7/c;->r(Landroid/content/Context;Ljava/lang/String;Lkotlin/jvm/functions/Function1;)Ll0/v;
    move-result-object v0
    invoke-virtual {v0}, Ll0/v;->c()Landroid/app/Notification;
    move-result-object v0
    new-instance v1, Ll0/a0;
    invoke-direct {v1, v4}, Ll0/a0;-><init>(Landroid/content/Context;)V
    invoke-virtual {v1, v9, v0}, Ll0/a0;->c(ILandroid/app/Notification;)V
    goto :goto_1f2
    :cond_1f1
    move-object v10, v5
    :goto_1f2
    if-eqz v8, :cond_218
    if-eq v8, v7, :cond_20c
    const/4 v14, 0x2
    if-eq v8, v14, :cond_205
    const/4 v10, 0x3
    if-eq v8, v10, :cond_1fe
    goto/16 :goto_28f
    :cond_1fe
    const-string v0, "\u5468\u62a5"
    invoke-static {v0, v2, v3}, Landroidx/work/impl/v;->w0(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    goto/16 :goto_28f
    :cond_205
    const-string v0, "\u5ba0\u7269\u51fa\u884c\u56de\u5bb6"
    invoke-static {v0, v2, v3}, Landroidx/work/impl/v;->w0(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    goto/16 :goto_28f
    :cond_20c
    const-string v0, "\u80af\u5b9a\u8bed"
    invoke-static {v0, v2, v3}, Landroidx/work/impl/v;->w0(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    :try_start_211
    new-instance v0, Lcom/nieruo/healthapp/workers/g;
    invoke-direct {v0, v4}, Lcom/nieruo/healthapp/workers/g;-><init>(Landroid/content/Context;)V
    :try_end_216
    .catchall {:try_start_211 .. :try_end_216} :catchall_28f
    goto/16 :goto_28f
    :cond_218
    invoke-virtual {v3}, Ljava/lang/String;->length()I
    move-result v0
    if-nez v0, :cond_220
    move/from16 v16, v7
    :cond_220
    if-eqz v16, :cond_223
    move-object v3, v2
    :cond_223
    const-string v0, "\u76ee\u6807\u6253\u5361\u63d0\u9192"
    invoke-static {v0, v2, v3}, Landroidx/work/impl/v;->w0(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V
    :try_start_228
    aget-object v0, v15, v7
    sget-object v0, Lcom/orhanobut/hawk/c;->a:Lcom/orhanobut/hawk/h;
    const/4 v1, 0x0
    invoke-interface {v0, v1, v10}, Lcom/orhanobut/hawk/h;->n(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/entity/ScheduleEntity;
    if-eqz v0, :cond_28f
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/ScheduleEntity;->getScheduleList()Ljava/util/List;
    move-result-object v0
    if-eqz v0, :cond_28f
    invoke-interface {v0}, Ljava/lang/Iterable;->iterator()Ljava/util/Iterator;
    move-result-object v0
    :cond_23f
    invoke-interface {v0}, Ljava/util/Iterator;->hasNext()Z
    move-result v2
    if-eqz v2, :cond_256
    invoke-interface {v0}, Ljava/util/Iterator;->next()Ljava/lang/Object;
    move-result-object v2
    move-object v3, v2
    check-cast v3, Lcom/nieruo/healthapp/entity/ScheduleItem;
    invoke-virtual {v3}, Lcom/nieruo/healthapp/entity/ScheduleItem;->getRequestCode()I
    move-result v3
    if-ne v3, v9, :cond_23f
    move-object v13, v2
    goto :goto_257
    :catch_254
    move-exception v0
    goto :goto_28c
    :cond_256
    move-object v13, v1
    :goto_257
    check-cast v13, Lcom/nieruo/healthapp/entity/ScheduleItem;
    if-eqz v13, :cond_28f
    invoke-static {}, Lcom/nieruo/healthapp/realm/g;->a()Lsj/e;
    move-result-object v0
    invoke-virtual {v13}, Lcom/nieruo/healthapp/entity/ScheduleItem;->getPrimaryKey()Lmo/i0;
    move-result-object v1
    filled-new-array {v1}, [Ljava/lang/Object;
    move-result-object v1
    invoke-static {v11}, Lkotlin/jvm/internal/Reflection;->getOrCreateKotlinClass(Ljava/lang/Class;)Lxk/d;
    move-result-object v2
    invoke-static {v1, v7}, Ljava/util/Arrays;->copyOf([Ljava/lang/Object;I)[Ljava/lang/Object;
    move-result-object v1
    check-cast v0, Lio/realm/kotlin/internal/g1;
    invoke-virtual {v0, v2, v12, v1}, Lio/realm/kotlin/internal/g1;->i(Lxk/d;Ljava/lang/String;[Ljava/lang/Object;)Lio/realm/kotlin/internal/query/c;
    move-result-object v0
    invoke-virtual {v0}, Lio/realm/kotlin/internal/query/c;->first()Ldk/d;
    move-result-object v0
    check-cast v0, Lio/realm/kotlin/internal/query/j;
    invoke-virtual {v0}, Lio/realm/kotlin/internal/query/j;->a()Lgk/a;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/realm/entity/Goal;
    if-eqz v0, :cond_28f
    new-instance v1, Lcom/nieruo/healthapp/workers/g;
    invoke-direct {v1, v4}, Lcom/nieruo/healthapp/workers/g;-><init>(Landroid/content/Context;)V
    invoke-virtual {v1, v0, v7}, Lcom/nieruo/healthapp/workers/g;->l(Lcom/nieruo/healthapp/realm/entity/Goal;Z)V
    :try_end_28b
    .catch Ljava/lang/Exception; {:try_start_228 .. :try_end_28b} :catch_254
    goto :goto_28f
    :goto_28c
    invoke-virtual {v0}, Ljava/lang/Throwable;->getMessage()Ljava/lang/String;
    :catchall_28f
    :cond_28f
    :goto_28f
    return-void
