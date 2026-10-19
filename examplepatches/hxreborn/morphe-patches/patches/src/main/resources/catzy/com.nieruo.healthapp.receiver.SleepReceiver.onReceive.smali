    const/4 v0, 0x0
    if-eqz p2, :cond_a
    const-string v1, "key_change_play_state"
    invoke-virtual {p2, v1, v0}, Landroid/content/Intent;->getBooleanExtra(Ljava/lang/String;Z)Z
    move-result v1
    goto :goto_b
    :cond_a
    move v1, v0
    :goto_b
    sget-object v2, Leh/a;->b:Leh/d;
    if-eqz v1, :cond_47
    sget-object v1, Lsi/c;->a:Ljava/util/ArrayList;
    invoke-static {}, Lsi/c;->d()Z
    move-result v1
    if-eqz v1, :cond_1b
    invoke-static {}, Lsi/c;->e()V
    goto :goto_31
    :cond_1b
    sget-object v1, Lcom/nieruo/healthapp/manager/a1;->m:Lfe/d;
    invoke-virtual {v1}, Lfe/d;->t()Lcom/nieruo/healthapp/manager/a1;
    move-result-object v1
    sget-object v3, Lcom/nieruo/healthapp/utils/x0;->a:Lcom/nieruo/healthapp/utils/x0;
    invoke-virtual {v3}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    sget-boolean v3, Lcom/nieruo/healthapp/utils/x0;->g:Z
    invoke-virtual {v1, v3}, Lcom/nieruo/healthapp/manager/a1;->Z(Z)Z
    move-result v1
    if-nez v1, :cond_31
    invoke-static {}, Lsi/c;->l()V
    :cond_31
    :goto_31
    invoke-virtual {v2}, Leh/d;->a()Leh/a;
    move-result-object v1
    const-class v3, Lcom/nieruo/healthapp/ui/activity/HomeActivity;
    invoke-virtual {v1, v3}, Leh/a;->e(Ljava/lang/Class;)Landroid/app/Activity;
    move-result-object v1
    if-nez v1, :cond_47
    if-eqz p1, :cond_47
    sget-object v1, Lcom/nieruo/healthapp/utils/x0;->a:Lcom/nieruo/healthapp/utils/x0;
    invoke-virtual {v1}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    invoke-static {p1}, Lcom/nieruo/healthapp/utils/x0;->i(Landroid/content/Context;)V
    :cond_47
    if-eqz p2, :cond_172
    invoke-virtual {p2}, Landroid/content/Intent;->getExtras()Landroid/os/Bundle;
    move-result-object p2
    if-eqz p2, :cond_172
    const-string v1, "key_show_sleep_alarm"
    invoke-virtual {p2, v1}, Landroid/os/BaseBundle;->getBoolean(Ljava/lang/String;)Z
    move-result p2
    const/4 v1, 0x1
    if-ne p2, v1, :cond_172
    sget-object p2, Lcom/nieruo/healthapp/manager/h;->g0:Lfe/d;
    invoke-virtual {p2}, Lfe/d;->s()Lcom/nieruo/healthapp/manager/h;
    move-result-object p2
    invoke-virtual {p2}, Lcom/nieruo/healthapp/manager/h;->d()V
    if-eqz p1, :cond_172
    sget-object p2, Lcom/nieruo/healthapp/utils/x0;->a:Lcom/nieruo/healthapp/utils/x0;
    sget-object v3, Lcom/nieruo/healthapp/receiver/SleepReceiver;->a:[Lxk/y;
    aget-object v4, v3, v0
    sget-object v4, Lcom/orhanobut/hawk/c;->a:Lcom/orhanobut/hawk/h;
    const-string v5, "temp_sleep_v2"
    const/4 v6, 0x0
    invoke-interface {v4, v6, v5}, Lcom/orhanobut/hawk/h;->n(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v4
    check-cast v4, Lcom/nieruo/healthapp/entity/TempSleepEntity;
    invoke-virtual {p2}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    invoke-static {v4}, Lcom/nieruo/healthapp/utils/x0;->b(Lcom/nieruo/healthapp/entity/TempSleepEntity;)Z
    move-result v4
    if-eqz v4, :cond_172
    aget-object v3, v3, v0
    sget-object v3, Lcom/orhanobut/hawk/c;->a:Lcom/orhanobut/hawk/h;
    invoke-interface {v3, v6, v5}, Lcom/orhanobut/hawk/h;->n(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v3
    check-cast v3, Lcom/nieruo/healthapp/entity/TempSleepEntity;
    if-eqz v3, :cond_8e
    invoke-virtual {v3}, Lcom/nieruo/healthapp/entity/TempSleepEntity;->getEnd_dt()J
    move-result-wide v3
    goto :goto_b0
    :cond_8e
    sget-object v3, Lkotlinx/datetime/w;->Companion:Lkotlinx/datetime/v;
    invoke-virtual {v3}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    new-instance v3, Lkotlinx/datetime/w;
    invoke-static {}, Lj$/time/Clock;->systemUTC()Lj$/time/Clock;
    move-result-object v4
    invoke-virtual {v4}, Lj$/time/Clock;->instant()Lj$/time/Instant;
    move-result-object v4
    invoke-direct {v3, v4}, Lkotlinx/datetime/w;-><init>(Lj$/time/Instant;)V
    sget-object v4, Lkotlinx/datetime/i0;->Companion:Lkotlinx/datetime/h0;
    invoke-static {v4, v3}, Lcom/nieruo/healthapp/d;->q(Lkotlinx/datetime/h0;Lkotlinx/datetime/w;)Lkotlinx/datetime/e0;
    move-result-object v3
    sget-object v4, Lkotlinx/datetime/i0;->b:Lkotlinx/datetime/t;
    invoke-static {v3, v4}, Landroidx/work/impl/model/f;->D(Lkotlinx/datetime/e0;Lkotlinx/datetime/i0;)Lkotlinx/datetime/w;
    move-result-object v3
    invoke-virtual {v3}, Lkotlinx/datetime/w;->c()J
    move-result-wide v3
    :goto_b0
    sget-object v5, Lkotlinx/datetime/w;->Companion:Lkotlinx/datetime/v;
    invoke-virtual {v5}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    invoke-static {v3, v4}, Lkotlinx/datetime/v;->a(J)Lkotlinx/datetime/w;
    move-result-object v3
    sget-object v4, Lkotlinx/datetime/i0;->Companion:Lkotlinx/datetime/h0;
    invoke-virtual {v4}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    sget-object v4, Lkotlinx/datetime/i0;->b:Lkotlinx/datetime/t;
    invoke-static {v3, v4}, Landroidx/work/impl/model/f;->E(Lkotlinx/datetime/w;Lkotlinx/datetime/i0;)Lkotlinx/datetime/e0;
    move-result-object v3
    invoke-virtual {v3}, Lkotlinx/datetime/e0;->b()Lkotlinx/datetime/g0;
    move-result-object v3
    sget-object v4, Lsi/c;->a:Ljava/util/ArrayList;
    invoke-static {}, Lsi/c;->i()V
    sget-object v4, Lcom/nieruo/healthapp/utils/x0;->c:Landroidx/media3/exoplayer/g0;
    if-eqz v4, :cond_d6
    invoke-virtual {v4}, Landroidx/core/view/p1;->n()Z
    move-result v4
    goto :goto_d7
    :cond_d6
    move v4, v0
    :goto_d7
    if-eqz v4, :cond_da
    goto :goto_147
    :cond_da
    invoke-static {}, Lcom/nieruo/healthapp/utils/x0;->f()V
    sget-object v4, Lcom/nieruo/healthapp/realm/dao/o1;->a:Lcom/nieruo/healthapp/realm/dao/c;
    invoke-virtual {v4}, Lcom/nieruo/healthapp/realm/dao/c;->h()Lcom/nieruo/healthapp/realm/dao/o1;
    move-result-object v4
    invoke-virtual {v4}, Lcom/nieruo/healthapp/realm/dao/o1;->c()Lcom/nieruo/healthapp/realm/entity/SleepSetting;
    move-result-object v4
    invoke-virtual {v4}, Lcom/nieruo/healthapp/realm/entity/SleepSetting;->f()Z
    move-result v5
    if-nez v5, :cond_ee
    goto :goto_147
    :cond_ee
    invoke-virtual {v4}, Lcom/nieruo/healthapp/realm/entity/SleepSetting;->g()I
    move-result v5
    invoke-virtual {v4}, Lcom/nieruo/healthapp/realm/entity/SleepSetting;->h()F
    if-lez v5, :cond_126
    invoke-static {}, Loi/a;->a()Lsj/e;
    move-result-object v6
    invoke-static {v5}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v5
    filled-new-array {v5}, [Ljava/lang/Object;
    move-result-object v5
    const-class v7, Lcom/nieruo/healthapp/resource/entry/RingtonesEntry;
    invoke-static {v7}, Lkotlin/jvm/internal/Reflection;->getOrCreateKotlinClass(Ljava/lang/Class;)Lxk/d;
    move-result-object v7
    invoke-static {v5, v1}, Ljava/util/Arrays;->copyOf([Ljava/lang/Object;I)[Ljava/lang/Object;
    move-result-object v5
    check-cast v6, Lio/realm/kotlin/internal/g1;
    const-string v8, "id == $0"
    invoke-virtual {v6, v7, v8, v5}, Lio/realm/kotlin/internal/g1;->i(Lxk/d;Ljava/lang/String;[Ljava/lang/Object;)Lio/realm/kotlin/internal/query/c;
    move-result-object v5
    invoke-virtual {v5}, Lio/realm/kotlin/internal/query/c;->first()Ldk/d;
    move-result-object v5
    check-cast v5, Lio/realm/kotlin/internal/query/j;
    invoke-virtual {v5}, Lio/realm/kotlin/internal/query/j;->a()Lgk/a;
    move-result-object v5
    check-cast v5, Lcom/nieruo/healthapp/resource/entry/RingtonesEntry;
    if-eqz v5, :cond_126
    invoke-virtual {p2, p1, v5}, Lcom/nieruo/healthapp/utils/x0;->e(Landroid/content/Context;Lcom/nieruo/healthapp/resource/entry/RingtonesEntry;)V
    :cond_126
    sget-boolean p2, Lcom/nieruo/healthapp/utils/x0;->e:Z
    if-eqz p2, :cond_12b
    goto :goto_147
    :cond_12b
    invoke-virtual {v4}, Lcom/nieruo/healthapp/realm/entity/SleepSetting;->k()Z
    move-result p2
    if-eqz p2, :cond_145
    invoke-static {}, Lp1/a;->m()Landroid/app/Application;
    move-result-object p2
    const/4 v4, 0x4
    new-array v4, v4, [J
    fill-array-data v4, :array_174
    const/16 v5, 0xc8
    filled-new-array {v5, v0, v5, v0}, [I
    move-result-object v0
    const/4 v5, 0x2
    invoke-static {p2, v4, v0, v5, v1}, Lbh/f;->f(Landroid/content/Context;[J[IIZ)V
    :cond_145
    sput-boolean v1, Lcom/nieruo/healthapp/utils/x0;->e:Z
    :goto_147
    invoke-virtual {v2}, Leh/d;->a()Leh/a;
    move-result-object p2
    const-class v0, Lcom/nieruo/healthapp/ui/activity/SleepAlarmActivity;
    invoke-virtual {p2, v0}, Leh/a;->e(Ljava/lang/Class;)Landroid/app/Activity;
    move-result-object p2
    if-nez p2, :cond_172
    invoke-static {}, Lcom/nieruo/healthapp/utils/t;->P0()Lkotlinx/datetime/format/h1;
    move-result-object p2
    invoke-virtual {p2, v3}, Lkotlinx/datetime/format/a;->a(Ljava/lang/Object;)Ljava/lang/String;
    move-result-object p2
    invoke-static {p1, p2}, Lcom/nieruo/healthapp/utils/x0;->k(Landroid/content/Context;Ljava/lang/String;)V
    new-instance p2, Landroid/content/Intent;
    invoke-direct {p2, p1, v0}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
    invoke-virtual {p1}, Landroid/content/Context;->getPackageName()Ljava/lang/String;
    move-result-object v0
    invoke-virtual {p2, v0}, Landroid/content/Intent;->setPackage(Ljava/lang/String;)Landroid/content/Intent;
    const/high16 v0, 0x10000000
    invoke-virtual {p2, v0}, Landroid/content/Intent;->addFlags(I)Landroid/content/Intent;
    invoke-virtual {p1, p2}, Landroid/content/Context;->startActivity(Landroid/content/Intent;)V
    :cond_172
    return-void
    nop
    :array_174
    .array-data 8
        0x1f4
        0x3e8
        0x1f4
        0x3e8
    .end array-data
