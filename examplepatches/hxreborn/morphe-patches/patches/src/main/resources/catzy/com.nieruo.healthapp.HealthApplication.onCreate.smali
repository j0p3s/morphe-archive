    move-object/from16 v1, p0
    const/16 v2, 0x8
    const/16 v3, 0x9
    const/4 v4, 0x2
    const/4 v5, 0x0
    const/4 v6, 0x1
    invoke-super {v1}, Landroid/app/Application;->onCreate()V
    invoke-static {}, Lp1/a;->m()Landroid/app/Application;
    move-result-object v0
    invoke-virtual {v0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;
    move-result-object v0
    invoke-static {}, Ln0/d;->q()Ljava/lang/String;
    move-result-object v7
    invoke-virtual {v0, v7}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :cond_20
    goto/16 :goto_4e8
    :cond_20
    invoke-static {v1}, Lp1/a;->t(Landroid/app/Application;)V
    sget-object v0, Lcom/nieruo/healthapp/manager/m1;->l:Lkc/f;
    invoke-virtual {v0}, Lkc/f;->Q()Lcom/nieruo/healthapp/manager/m1;
    move-result-object v0
    iput-object v1, v0, Lcom/nieruo/healthapp/manager/m1;->a:Lcom/nieruo/healthapp/HealthApplication;
    new-instance v7, Llg/a;
    invoke-direct {v7, v1}, Lcom/hypersoft/billing/controller/c;-><init>(Landroid/content/Context;)V
    iput-object v7, v0, Lcom/nieruo/healthapp/manager/m1;->b:Llg/a;
    sget-object v7, Lng/a;->b:Landroidx/lifecycle/q0;
    iget-object v8, v0, Lcom/nieruo/healthapp/manager/m1;->k:Lcom/nieruo/healthapp/manager/b1;
    invoke-virtual {v7, v8}, Landroidx/lifecycle/n0;->f(Landroidx/lifecycle/r0;)V
    invoke-virtual {v0}, Lcom/nieruo/healthapp/manager/m1;->d()Z
    move-result v7
    if-nez v7, :cond_40
    goto :goto_52
    :cond_40
    iget-object v8, v0, Lcom/nieruo/healthapp/manager/m1;->b:Llg/a;
    if-eqz v8, :cond_52
    sget-object v9, Lkotlin/collections/e0;->INSTANCE:Lkotlin/collections/e0;
    new-instance v12, Landroidx/work/impl/o;
    invoke-direct {v12, v0, v3}, Landroidx/work/impl/o;-><init>(Ljava/lang/Object;I)V
    const/16 v13, 0x10
    move-object v10, v9
    move-object v11, v9
    invoke-static/range {v8 .. v13}, Llg/a;->n(Llg/a;Ljava/util/List;Ljava/util/List;Ljava/util/List;Landroidx/work/impl/o;I)V
    :cond_52
    :goto_52
    sget v0, Lcom/nieruo/healthapp/utils/f;->a:I
    new-instance v0, Lcom/nieruo/healthapp/b;
    invoke-direct {v0}, Ljava/lang/Object;-><init>()V
    new-instance v7, Lcom/google/android/gms/internal/measurement/k1;
    invoke-direct {v7, v0, v6}, Lcom/google/android/gms/internal/measurement/k1;-><init>(Ljava/lang/Object;I)V
    invoke-virtual {v1, v7}, Landroid/app/Application;->registerActivityLifecycleCallbacks(Landroid/app/Application$ActivityLifecycleCallbacks;)V
    const-string v0, "initHawk"
    invoke-static {v0}, Lqe/b;->a(Ljava/lang/String;)Lcom/google/firebase/perf/metrics/Trace;
    move-result-object v0
    invoke-static {v1}, Ln0/d;->y(Landroid/content/Context;)V
    invoke-virtual {v0}, Lcom/google/firebase/perf/metrics/Trace;->stop()V
    sget-object v0, Lcom/nieruo/core/common/base/DebugActionRegistry;->INSTANCE:Lcom/nieruo/core/common/base/DebugActionRegistry;
    new-instance v7, Landroidx/window/layout/o;
    invoke-direct {v7, v6}, Landroidx/window/layout/o;-><init>(I)V
    invoke-virtual {v0, v7}, Lcom/nieruo/core/common/base/DebugActionRegistry;->setOnUnlockPassClicked(Lkotlin/jvm/functions/Function1;)V
    new-instance v7, Lcom/nieruo/core/common/oss/a;
    const/16 v6, 0x4
    invoke-direct {v7, v6}, Lcom/nieruo/core/common/oss/a;-><init>(I)V
    const/16 v6, 0x1
    invoke-virtual {v0, v7}, Lcom/nieruo/core/common/base/DebugActionRegistry;->setOnIncreaseIntimacyClicked(Lkotlin/jvm/functions/Function0;)V
    sget-object v7, Lcom/nieruo/healthapp/HealthApplication;->b:[Lxk/y;
    aget-object v0, v7, v6
    sget-object v0, Lcom/orhanobut/hawk/c;->a:Lcom/orhanobut/hawk/h;
    const-string v8, "startup_count"
    const/4 v9, 0x0
    invoke-interface {v0, v9, v8}, Lcom/orhanobut/hawk/h;->n(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/lang/Integer;
    if-eqz v0, :cond_95
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I
    move-result v0
    goto :goto_96
    :cond_95
    move v0, v5
    :goto_96
    add-int/lit8 v8, v0, 0x1
    sget-object v0, Lkotlinx/datetime/i0;->Companion:Lkotlinx/datetime/h0;
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    invoke-static {}, Lkotlinx/datetime/h0;->a()Lkotlinx/datetime/i0;
    move-result-object v0
    sget-object v10, Lkotlinx/datetime/w;->Companion:Lkotlinx/datetime/v;
    invoke-virtual {v10}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    new-instance v10, Lkotlinx/datetime/w;
    invoke-static {}, Lj$/time/Clock;->systemUTC()Lj$/time/Clock;
    move-result-object v11
    invoke-virtual {v11}, Lj$/time/Clock;->instant()Lj$/time/Instant;
    move-result-object v11
    invoke-direct {v10, v11}, Lkotlinx/datetime/w;-><init>(Lj$/time/Instant;)V
    invoke-static {v10, v0, v5, v5, v5}, Lcom/nieruo/healthapp/d;->p(Lkotlinx/datetime/w;Lkotlinx/datetime/i0;III)Lkotlinx/datetime/e0;
    move-result-object v0
    sget-object v10, Lkotlinx/datetime/i0;->b:Lkotlinx/datetime/t;
    invoke-static {v0, v10}, Landroidx/work/impl/model/f;->D(Lkotlinx/datetime/e0;Lkotlinx/datetime/i0;)Lkotlinx/datetime/w;
    move-result-object v0
    invoke-virtual {v0}, Lkotlinx/datetime/w;->c()J
    move-result-wide v10
    aget-object v0, v7, v4
    sget-object v0, Lcom/orhanobut/hawk/c;->a:Lcom/orhanobut/hawk/h;
    const-string v12, "startup_daily_entity"
    invoke-interface {v0, v9, v12}, Lcom/orhanobut/hawk/h;->n(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/entity/StartupDailyEntity;
    if-eqz v0, :cond_d4
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/StartupDailyEntity;->getCount()I
    move-result v0
    goto :goto_d5
    :cond_d4
    move v0, v5
    :goto_d5
    aget-object v13, v7, v4
    sget-object v13, Lcom/orhanobut/hawk/c;->a:Lcom/orhanobut/hawk/h;
    invoke-interface {v13, v9, v12}, Lcom/orhanobut/hawk/h;->n(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v12
    check-cast v12, Lcom/nieruo/healthapp/entity/StartupDailyEntity;
    if-eqz v12, :cond_eb
    invoke-virtual {v12}, Lcom/nieruo/healthapp/entity/StartupDailyEntity;->getLastTime()J
    move-result-wide v12
    cmp-long v10, v10, v12
    if-nez v10, :cond_eb
    :goto_e9
    move v10, v0
    goto :goto_ed
    :cond_eb
    add-int/2addr v0, v6
    goto :goto_e9
    :goto_ed
    sget-object v11, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    invoke-static {v1}, Leh/g;->b(Landroid/content/Context;)Ljava/util/Locale;
    move-result-object v0
    invoke-static {v0}, Luh/c;->c(Ljava/util/Locale;)V
    sget-object v12, Luh/c;->d:Ljava/util/LinkedHashMap;
    const-string v0, "System"
    const-string v13, "android"
    invoke-interface {v12, v0, v13}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    const-string v0, "ApiVersion"
    const-string v13, "1"
    invoke-interface {v12, v0, v13}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-static {}, Lp1/a;->m()Landroid/app/Application;
    move-result-object v0
    invoke-virtual {v0}, Landroid/content/Context;->getPackageName()Ljava/lang/String;
    move-result-object v0
    invoke-static {v0}, Lqd/z1;->C(Ljava/lang/String;)Z
    move-result v13
    if-eqz v13, :cond_115
    goto :goto_12b
    :cond_115
    :try_start_115
    invoke-static {}, Lp1/a;->m()Landroid/app/Application;
    move-result-object v13
    invoke-virtual {v13}, Landroid/content/Context;->getPackageManager()Landroid/content/pm/PackageManager;
    move-result-object v13
    invoke-virtual {v13, v0, v5}, Landroid/content/pm/PackageManager;->getPackageInfo(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;
    move-result-object v0
    if-nez v0, :cond_124
    goto :goto_12b
    :cond_124
    iget-object v0, v0, Landroid/content/pm/PackageInfo;->versionName:Ljava/lang/String;
    :try_end_126
    .catch Landroid/content/pm/PackageManager$NameNotFoundException; {:try_start_115 .. :try_end_126} :catch_127
    goto :goto_12d
    :catch_127
    move-exception v0
    invoke-virtual {v0}, Ljava/lang/Throwable;->printStackTrace()V
    :goto_12b
    const-string v0, ""
    :goto_12d
    invoke-static {v0}, Lkotlin/jvm/internal/Intrinsics;->checkNotNull(Ljava/lang/Object;)V
    const-string v13, "AppVersion"
    invoke-interface {v12, v13, v0}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    const-string v0, "Channel"
    const-string v13, "google"
    invoke-interface {v12, v0, v13}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    const-string v0, "Accept"
    const-string v13, "application/json"
    invoke-interface {v12, v0, v13}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    const-string v0, "Package"
    invoke-virtual {v1}, Landroid/content/Context;->getPackageName()Ljava/lang/String;
    move-result-object v13
    invoke-interface {v12, v0, v13}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    const-string v0, "Content-Type"
    const-string v13, "application/x-www-form-urlencoded"
    invoke-interface {v12, v0, v13}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-static {}, Luh/c;->a()Ljava/lang/String;
    move-result-object v0
    const-string v13, "Accept-Language"
    invoke-interface {v12, v13, v0}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-static {v8}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v0
    const-string v8, "Startup-Count"
    invoke-interface {v12, v8, v0}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-static {v10}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v0
    const-string v8, "Startup-Daily-Count"
    invoke-interface {v12, v8, v0}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-static {}, Ljava/util/TimeZone;->getDefault()Ljava/util/TimeZone;
    move-result-object v0
    invoke-virtual {v0}, Ljava/util/TimeZone;->getRawOffset()I
    move-result v0
    div-int/lit16 v0, v0, 0x3e8
    invoke-static {v0}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v0
    const-string v8, "time_difference"
    invoke-interface {v12, v8, v0}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    sget-object v0, Luh/c;->c:Ljava/lang/String;
    const-string v8, "PayCountry"
    invoke-interface {v12, v8, v0}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    sget-object v0, Luh/c;->b:Ljava/lang/String;
    const-string v8, "Country"
    invoke-interface {v12, v8, v0}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-static {}, Lcom/nieruo/healthapp/utils/t;->O()Ljava/lang/String;
    move-result-object v0
    invoke-virtual {v0}, Ljava/lang/String;->length()I
    move-result v8
    if-lez v8, :cond_1a0
    sput-object v0, Luh/c;->b:Ljava/lang/String;
    const-string v8, "Country"
    invoke-interface {v12, v8, v0}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    :cond_1a0
    const-string v0, "initNt"
    invoke-static {v0}, Lqe/b;->a(Ljava/lang/String;)Lcom/google/firebase/perf/metrics/Trace;
    move-result-object v0
    sget-object v8, Lcom/nieruo/params/ParamsConfig;->a:Lfe/d;
    invoke-virtual {v8}, Lfe/d;->v()Lcom/nieruo/params/ParamsConfig;
    move-result-object v10
    invoke-virtual {v10}, Lcom/nieruo/params/ParamsConfig;->l()Ljava/lang/String;
    move-result-object v13
    invoke-virtual {v8}, Lfe/d;->v()Lcom/nieruo/params/ParamsConfig;
    move-result-object v10
    invoke-virtual {v10}, Lcom/nieruo/params/ParamsConfig;->m()Ljava/lang/String;
    move-result-object v14
    sget-object v17, Lcom/zt/analytics/sdk/config/ZTChannelConfig;->GOOGLE:Lcom/zt/analytics/sdk/config/ZTChannelConfig;
    invoke-virtual {v8}, Lfe/d;->v()Lcom/nieruo/params/ParamsConfig;
    move-result-object v10
    invoke-virtual {v10}, Lcom/nieruo/params/ParamsConfig;->n()Ljava/lang/String;
    move-result-object v15
    sget-object v10, Lcom/nieruo/healthapp/manager/a1;->m:Lfe/d;
    invoke-virtual {v10}, Lfe/d;->t()Lcom/nieruo/healthapp/manager/a1;
    move-result-object v12
    invoke-virtual {v12}, Lcom/nieruo/healthapp/manager/a1;->D()Z
    move-result v12
    invoke-virtual {v10}, Lfe/d;->t()Lcom/nieruo/healthapp/manager/a1;
    move-result-object v16
    invoke-virtual/range {v16 .. v16}, Lcom/nieruo/healthapp/manager/a1;->t()Ljava/lang/String;
    move-result-object v16
    if-eqz v16, :cond_1e5
    invoke-virtual {v10}, Lfe/d;->t()Lcom/nieruo/healthapp/manager/a1;
    move-result-object v10
    invoke-virtual {v10}, Lcom/nieruo/healthapp/manager/a1;->t()Ljava/lang/String;
    move-result-object v10
    invoke-static {v10}, Lkotlin/jvm/internal/Intrinsics;->checkNotNull(Ljava/lang/Object;)V
    move-object/from16 v18, v10
    :goto_1e3
    move v10, v12
    goto :goto_1e8
    :cond_1e5
    move-object/from16 v18, v9
    goto :goto_1e3
    :goto_1e8
    new-instance v12, Lcom/zt/analytics/sdk/config/ZTInitConfig;
    invoke-static {v10}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v19
    const/16 v23, 0x380
    const/16 v24, 0x0
    const/16 v16, 0x1
    const/16 v20, 0x0
    const/16 v21, 0x0
    const/16 v22, 0x0
    invoke-direct/range {v12 .. v24}, Lcom/zt/analytics/sdk/config/ZTInitConfig;-><init>(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLcom/zt/analytics/sdk/config/ZTChannelConfig;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/String;Ljava/util/List;ZILkotlin/jvm/internal/DefaultConstructorMarker;)V
    invoke-static {v5}, Lcom/zt/analytics/sdk/ZTAnalytics;->setDebug(Z)V
    new-instance v10, Lcom/nieruo/core/common/oss/a;
    const/4 v4, 0x5
    invoke-direct {v10, v4}, Lcom/nieruo/core/common/oss/a;-><init>(I)V
    const/4 v4, 0x2
    new-instance v13, Landroidx/window/layout/o;
    const/4 v14, 0x4
    invoke-direct {v13, v14}, Landroidx/window/layout/o;-><init>(I)V
    invoke-static {v12, v10, v13}, Lcom/zt/analytics/sdk/ZTAnalytics;->init(Lcom/zt/analytics/sdk/config/ZTInitConfig;Lkotlin/jvm/functions/Function0;Lkotlin/jvm/functions/Function1;)V
    new-instance v15, Lcom/zt/analytics/sdk/thirdparty/ZTTAConfig;
    invoke-virtual {v8}, Lfe/d;->v()Lcom/nieruo/params/ParamsConfig;
    move-result-object v10
    invoke-virtual {v10}, Lcom/nieruo/params/ParamsConfig;->k()Ljava/lang/String;
    move-result-object v16
    invoke-virtual {v8}, Lfe/d;->v()Lcom/nieruo/params/ParamsConfig;
    move-result-object v10
    invoke-virtual {v10}, Lcom/nieruo/params/ParamsConfig;->j()Ljava/lang/String;
    move-result-object v17
    const/16 v22, 0x28
    const/16 v23, 0x0
    const/16 v18, 0x1
    const/16 v19, 0x0
    const/16 v20, 0x0
    const/16 v21, 0x0
    invoke-direct/range {v15 .. v23}, Lcom/zt/analytics/sdk/thirdparty/ZTTAConfig;-><init>(Ljava/lang/String;Ljava/lang/String;ZZZLjava/lang/Integer;ILkotlin/jvm/internal/DefaultConstructorMarker;)V
    invoke-static {v15, v9, v4, v9}, Lcom/zt/analytics/sdk/ZTAnalytics;->enableThinkingData$default(Lcom/zt/analytics/sdk/thirdparty/ZTTAConfig;Lkotlin/jvm/functions/Function1;ILjava/lang/Object;)V
    new-instance v16, Lcom/zt/analytics/sdk/config/ZTAdjustConfig;
    invoke-virtual {v8}, Lfe/d;->v()Lcom/nieruo/params/ParamsConfig;
    move-result-object v8
    invoke-virtual {v8}, Lcom/nieruo/params/ParamsConfig;->a()Ljava/lang/String;
    move-result-object v17
    new-instance v8, Lcom/nieruo/healthapp/a;
    invoke-direct {v8, v1, v5}, Lcom/nieruo/healthapp/a;-><init>(Landroid/content/ComponentCallbacks;I)V
    const/16 v21, 0x6
    const/16 v22, 0x0
    const/16 v18, 0x0
    move-object/from16 v20, v8
    invoke-direct/range {v16 .. v22}, Lcom/zt/analytics/sdk/config/ZTAdjustConfig;-><init>(Ljava/lang/String;ZZLkotlin/jvm/functions/Function2;ILkotlin/jvm/internal/DefaultConstructorMarker;)V
    invoke-static/range {v16 .. v16}, Lcom/zt/analytics/sdk/ZTAnalytics;->enableAdjust(Lcom/zt/analytics/sdk/config/ZTAdjustConfig;)V
    sget-object v8, Lcom/orhanobut/hawk/c;->a:Lcom/orhanobut/hawk/h;
    const-string v10, "adjust_attr_success"
    invoke-interface {v8, v10}, Lcom/orhanobut/hawk/h;->contains(Ljava/lang/String;)Z
    move-result v8
    if-eqz v8, :cond_25c
    invoke-virtual {v1}, Lcom/nieruo/healthapp/HealthApplication;->a()V
    :cond_25c
    invoke-virtual {v0}, Lcom/google/firebase/perf/metrics/Trace;->stop()V
    sget-object v0, Leh/e;->b:Leh/d;
    invoke-virtual {v0}, Leh/d;->c()Leh/e;
    move-result-object v0
    iget-object v0, v0, Leh/e;->a:Ljava/util/LinkedHashMap;
    invoke-virtual {v1}, Landroid/content/Context;->getAssets()Landroid/content/res/AssetManager;
    move-result-object v8
    const-string v10, "font/manrope_semibold.otf"
    invoke-static {v8, v10}, Landroid/graphics/Typeface;->createFromAsset(Landroid/content/res/AssetManager;Ljava/lang/String;)Landroid/graphics/Typeface;
    move-result-object v8
    const-string v10, "semibold"
    invoke-interface {v0, v10, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-virtual {v1}, Landroid/content/Context;->getAssets()Landroid/content/res/AssetManager;
    move-result-object v8
    const-string v10, "font/manrope_bold.otf"
    invoke-static {v8, v10}, Landroid/graphics/Typeface;->createFromAsset(Landroid/content/res/AssetManager;Ljava/lang/String;)Landroid/graphics/Typeface;
    move-result-object v8
    const-string v10, "bold"
    invoke-interface {v0, v10, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-virtual {v1}, Landroid/content/Context;->getAssets()Landroid/content/res/AssetManager;
    move-result-object v8
    const-string v10, "font/manrope_extrabold.otf"
    invoke-static {v8, v10}, Landroid/graphics/Typeface;->createFromAsset(Landroid/content/res/AssetManager;Ljava/lang/String;)Landroid/graphics/Typeface;
    move-result-object v8
    const-string v10, "extrabold"
    invoke-interface {v0, v10, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-virtual {v1}, Landroid/content/Context;->getAssets()Landroid/content/res/AssetManager;
    move-result-object v8
    const-string v10, "font/manrope_extralight.otf"
    invoke-static {v8, v10}, Landroid/graphics/Typeface;->createFromAsset(Landroid/content/res/AssetManager;Ljava/lang/String;)Landroid/graphics/Typeface;
    move-result-object v8
    const-string v10, "extralight"
    invoke-interface {v0, v10, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-virtual {v1}, Landroid/content/Context;->getAssets()Landroid/content/res/AssetManager;
    move-result-object v8
    const-string v10, "font/manrope_light.otf"
    invoke-static {v8, v10}, Landroid/graphics/Typeface;->createFromAsset(Landroid/content/res/AssetManager;Ljava/lang/String;)Landroid/graphics/Typeface;
    move-result-object v8
    const-string v10, "light"
    invoke-interface {v0, v10, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-virtual {v1}, Landroid/content/Context;->getAssets()Landroid/content/res/AssetManager;
    move-result-object v8
    const-string v10, "font/manrope_medium.otf"
    invoke-static {v8, v10}, Landroid/graphics/Typeface;->createFromAsset(Landroid/content/res/AssetManager;Ljava/lang/String;)Landroid/graphics/Typeface;
    move-result-object v8
    const-string v10, "medium"
    invoke-interface {v0, v10, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    invoke-virtual {v1}, Landroid/content/Context;->getAssets()Landroid/content/res/AssetManager;
    move-result-object v8
    const-string v10, "font/manrope_regular.otf"
    invoke-static {v8, v10}, Landroid/graphics/Typeface;->createFromAsset(Landroid/content/res/AssetManager;Ljava/lang/String;)Landroid/graphics/Typeface;
    move-result-object v8
    const-string v10, "regular"
    invoke-interface {v0, v10, v8}, Ljava/util/Map;->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
    new-instance v0, Lbh/h;
    invoke-direct {v0, v1, v4}, Lbh/h;-><init>(Ljava/lang/Object;I)V
    new-instance v4, Leh/b;
    invoke-direct {v4, v0}, Leh/b;-><init>(Lbh/h;)V
    invoke-virtual {v1, v4}, Landroid/app/Application;->registerActivityLifecycleCallbacks(Landroid/app/Application$ActivityLifecycleCallbacks;)V
    sget-object v0, Lcom/nieruo/healthapp/realm/g;->a:Lio/realm/kotlin/internal/g1;
    const-string v0, "initRealmTrace"
    invoke-static {v0}, Lqe/b;->a(Ljava/lang/String;)Lcom/google/firebase/perf/metrics/Trace;
    move-result-object v0
    sget-object v4, Lak/l;->a:Lyj/a;
    sget-object v4, Lak/g;->NONE:Lak/g;
    invoke-static {v4}, Lak/l;->a(Lak/g;)V
    const/4 v8, 0x3
    invoke-static {v1, v9, v8}, Lcom/nieruo/healthapp/realm/g;->c(Landroid/content/Context;Ljava/lang/String;I)Lio/realm/kotlin/internal/z0;
    move-result-object v10
    sput-object v10, Lcom/nieruo/healthapp/realm/g;->c:Lio/realm/kotlin/internal/z0;
    invoke-virtual {v0}, Lcom/google/firebase/perf/metrics/Trace;->stop()V
    sget-object v0, Loi/a;->a:Lik/t;
    const-string v0, "initResourceRealmTrace"
    invoke-static {v0}, Lqe/b;->a(Ljava/lang/String;)Lcom/google/firebase/perf/metrics/Trace;
    move-result-object v0
    invoke-static {v4}, Lak/l;->a(Lak/g;)V
    invoke-static {v1, v8}, Loi/a;->b(Landroid/app/Application;I)Lio/realm/kotlin/internal/z0;
    move-result-object v4
    sput-object v4, Loi/a;->b:Lio/realm/kotlin/internal/z0;
    invoke-virtual {v0}, Lcom/google/firebase/perf/metrics/Trace;->stop()V
    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I
    const/16 v4, 0x21
    if-lt v0, v4, :cond_354
    invoke-static {v1}, Leh/g;->b(Landroid/content/Context;)Ljava/util/Locale;
    move-result-object v8
    filled-new-array {v8}, [Ljava/util/Locale;
    move-result-object v8
    sget-object v10, Lv0/e;->b:Lv0/e;
    new-instance v10, Landroid/os/LocaleList;
    invoke-direct {v10, v8}, Landroid/os/LocaleList;-><init>([Ljava/util/Locale;)V
    new-instance v8, Lv0/e;
    new-instance v12, Lv0/f;
    invoke-direct {v12, v10}, Lv0/f;-><init>(Landroid/os/LocaleList;)V
    invoke-direct {v8, v12}, Lv0/e;-><init>(Lv0/f;)V
    sget-object v12, Li/q;->a:Landroidx/room/l0;
    if-lt v0, v4, :cond_33f
    invoke-static {}, Li/q;->b()Ljava/lang/Object;
    move-result-object v0
    if-eqz v0, :cond_354
    invoke-virtual {v10}, Landroid/os/LocaleList;->toLanguageTags()Ljava/lang/String;
    move-result-object v4
    invoke-static {v4}, Li/o;->a(Ljava/lang/String;)Landroid/os/LocaleList;
    move-result-object v4
    invoke-static {v0, v4}, Li/p;->b(Ljava/lang/Object;Landroid/os/LocaleList;)V
    goto :goto_354
    :cond_33f
    sget-object v0, Li/q;->c:Lv0/e;
    invoke-virtual {v8, v0}, Lv0/e;->equals(Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :cond_354
    sget-object v4, Li/q;->h:Ljava/lang/Object;
    monitor-enter v4
    :try_start_34a
    sput-object v8, Li/q;->c:Lv0/e;
    invoke-static {}, Li/q;->a()V
    monitor-exit v4
    goto :goto_354
    :catchall_351
    move-exception v0
    monitor-exit v4
    :try_end_353
    .catchall {:try_start_34a .. :try_end_353} :catchall_351
    throw v0
    :cond_354
    :goto_354
    sget-object v0, Landroidx/lifecycle/x0;->i:Landroidx/lifecycle/x0;
    invoke-static {v0}, Landroidx/lifecycle/i1;->d(Landroidx/lifecycle/e0;)Landroidx/lifecycle/y;
    move-result-object v0
    new-instance v4, Lcom/nieruo/healthapp/f;
    invoke-direct {v4, v1, v9}, Lcom/nieruo/healthapp/f;-><init>(Lcom/nieruo/healthapp/HealthApplication;Lkotlin/coroutines/d;)V
    invoke-static {v0, v4}, Lqd/z1;->F(Lkotlinx/coroutines/f0;Lkotlin/jvm/functions/Function2;)Lkotlinx/coroutines/e2;
    invoke-static {}, Ljd/c;->a()Ljd/c;
    move-result-object v0
    iget-object v0, v0, Ljd/c;->a:Lnd/r;
    iget-object v4, v0, Lnd/r;->b:Le0/e;
    monitor-enter v4
    :try_start_36b
    iput-boolean v5, v4, Le0/e;->b:Z
    iput-object v11, v4, Le0/e;->g:Ljava/lang/Object;
    iget-object v0, v4, Le0/e;->c:Ljava/lang/Object;
    check-cast v0, Landroid/content/SharedPreferences;
    invoke-interface {v0}, Landroid/content/SharedPreferences;->edit()Landroid/content/SharedPreferences$Editor;
    move-result-object v0
    const-string v8, "firebase_crashlytics_collection_enabled"
    invoke-interface {v0, v8, v6}, Landroid/content/SharedPreferences$Editor;->putBoolean(Ljava/lang/String;Z)Landroid/content/SharedPreferences$Editor;
    invoke-interface {v0}, Landroid/content/SharedPreferences$Editor;->apply()V
    iget-object v8, v4, Le0/e;->e:Ljava/lang/Object;
    monitor-enter v8
    :try_end_382
    .catchall {:try_start_36b .. :try_end_382} :catchall_4eb
    :try_start_382
    invoke-virtual {v4}, Le0/e;->f()Z
    move-result v0
    if-eqz v0, :cond_399
    iget-boolean v0, v4, Le0/e;->a:Z
    if-nez v0, :cond_3a6
    iget-object v0, v4, Le0/e;->f:Ljava/lang/Object;
    check-cast v0, Lhb/j;
    invoke-virtual {v0, v9}, Lhb/j;->d(Ljava/lang/Object;)V
    iput-boolean v6, v4, Le0/e;->a:Z
    goto :goto_3a6
    :catchall_396
    move-exception v0
    goto/16 :goto_4e9
    :cond_399
    iget-boolean v0, v4, Le0/e;->a:Z
    if-eqz v0, :cond_3a6
    new-instance v0, Lhb/j;
    invoke-direct {v0}, Lhb/j;-><init>()V
    iput-object v0, v4, Le0/e;->f:Ljava/lang/Object;
    iput-boolean v5, v4, Le0/e;->a:Z
    :cond_3a6
    :goto_3a6
    monitor-exit v8
    :try_end_3a7
    .catchall {:try_start_382 .. :try_end_3a7} :catchall_396
    monitor-exit v4
    invoke-static {}, Lcom/google/firebase/messaging/FirebaseMessaging;->c()Lcom/google/firebase/messaging/FirebaseMessaging;
    move-result-object v0
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    new-instance v4, Lhb/j;
    invoke-direct {v4}, Lhb/j;-><init>()V
    new-instance v8, La8/t;
    const/16 v3, 0xf
    invoke-direct {v8, v3, v0, v4}, La8/t;-><init>(ILjava/lang/Object;Ljava/lang/Object;)V
    iget-object v0, v0, Lcom/google/firebase/messaging/FirebaseMessaging;->f:Ljava/util/concurrent/ScheduledThreadPoolExecutor;
    invoke-virtual {v0, v8}, Ljava/util/concurrent/ScheduledThreadPoolExecutor;->execute(Ljava/lang/Runnable;)V
    iget-object v0, v4, Lhb/j;->a:Lhb/q;
    new-instance v3, Landroidx/work/impl/o;
    invoke-direct {v3, v1, v2}, Landroidx/work/impl/o;-><init>(Ljava/lang/Object;I)V
    invoke-virtual {v0, v3}, Lhb/q;->i(Lhb/d;)Lhb/q;
    const-string v0, "first_day_of_week"
    const/16 v3, 0x12c
    invoke-static {v3}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
    move-result-object v4
    aget-object v8, v7, v5
    sget-object v8, Lcom/orhanobut/hawk/c;->a:Lcom/orhanobut/hawk/h;
    invoke-interface {v8, v4, v0}, Lcom/orhanobut/hawk/h;->n(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v8
    check-cast v8, Ljava/lang/Integer;
    if-nez v8, :cond_3dd
    goto :goto_3e5
    :cond_3dd
    invoke-virtual {v8}, Ljava/lang/Integer;->intValue()I
    move-result v8
    if-ne v8, v3, :cond_3e5
    move v8, v3
    goto :goto_3e7
    :cond_3e5
    :goto_3e5
    const/16 v8, 0x12d
    :goto_3e7
    sput v8, Landroidx/work/o0;->b:I
    sget-object v8, Lcom/nieruo/healthapp/realm/dao/j0;->a:Lcom/nieruo/healthapp/realm/dao/g;
    aget-object v7, v7, v5
    sget-object v7, Lcom/orhanobut/hawk/c;->a:Lcom/orhanobut/hawk/h;
    invoke-interface {v7, v4, v0}, Lcom/orhanobut/hawk/h;->n(Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/Object;
    move-result-object v0
    check-cast v0, Ljava/lang/Integer;
    if-nez v0, :cond_3f8
    goto :goto_400
    :cond_3f8
    invoke-virtual {v0}, Ljava/lang/Integer;->intValue()I
    move-result v0
    if-eq v0, v3, :cond_3ff
    goto :goto_400
    :cond_3ff
    move v6, v5
    :goto_400
    invoke-virtual {v8}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    sput-boolean v6, Lcom/nieruo/healthapp/realm/dao/j0;->d:Z
    new-instance v0, Lzg/b;
    new-instance v3, Landroidx/work/impl/model/c;
    const/16 v4, 0x19
    invoke-direct {v3, v4}, Landroidx/work/impl/model/c;-><init>(I)V
    invoke-direct {v0, v1, v3}, Lzg/b;-><init>(Lcom/nieruo/healthapp/HealthApplication;Landroidx/work/impl/model/c;)V
    invoke-static {v0}, Ljava/lang/Thread;->setDefaultUncaughtExceptionHandler(Ljava/lang/Thread$UncaughtExceptionHandler;)V
    new-instance v0, La8/s;
    const/16 v2, 0x1c
    invoke-direct {v0, v2}, La8/s;-><init>(I)V
    sget-object v2, Lcom/nieruo/healthapp/utils/h0;->a:Lcom/nieruo/healthapp/utils/h0;
    invoke-virtual {v2}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    sget-object v3, Lcom/nieruo/healthapp/utils/h0;->b:[Lxk/y;
    aget-object v3, v3, v5
    sget-object v4, Lcom/nieruo/healthapp/utils/h0;->c:Ldh/a;
    invoke-virtual {v4, v2, v3}, Ldh/a;->a(Ljava/lang/Object;Lxk/y;)Ljava/lang/Object;
    move-result-object v2
    check-cast v2, Ljava/lang/Boolean;
    invoke-static {v2, v11}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v2
    if-nez v2, :cond_444
    invoke-static {}, Lp1/a;->m()Landroid/app/Application;
    move-result-object v2
    invoke-static {v2}, Lcom/android/installreferrer/api/InstallReferrerClient;->newBuilder(Landroid/content/Context;)Lcom/android/installreferrer/api/InstallReferrerClient$Builder;
    move-result-object v2
    invoke-virtual {v2}, Lcom/android/installreferrer/api/InstallReferrerClient$Builder;->build()Lcom/android/installreferrer/api/InstallReferrerClient;
    move-result-object v2
    new-instance v3, Lcom/nieruo/healthapp/ui/fragment/u6;
    invoke-direct {v3, v2, v0}, Lcom/nieruo/healthapp/ui/fragment/u6;-><init>(Lcom/android/installreferrer/api/InstallReferrerClient;La8/s;)V
    :try_start_441
    invoke-virtual {v2, v3}, Lcom/android/installreferrer/api/InstallReferrerClient;->startConnection(Lcom/android/installreferrer/api/InstallReferrerStateListener;)V
    :try_end_444
    .catch Ljava/lang/Exception; {:try_start_441 .. :try_end_444} :catch_444
    :catch_444
    :cond_444
    sget-object v0, Ljava/util/concurrent/TimeUnit;->DAYS:Ljava/util/concurrent/TimeUnit;
    new-instance v2, Landroidx/work/i0;
    const-class v3, Lcom/nieruo/healthapp/workers/AlarmReconcileWorker;
    invoke-direct {v2, v3}, Landroidx/work/p0;-><init>(Ljava/lang/Class;)V
    iget-object v3, v2, Landroidx/work/p0;->c:Landroidx/work/impl/model/r;
    const-wide/16 v4, 0x1
    invoke-virtual {v0, v4, v5}, Ljava/util/concurrent/TimeUnit;->toMillis(J)J
    move-result-wide v4
    invoke-virtual {v3}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    const-wide/32 v6, 0xdbba0
    cmp-long v0, v4, v6
    sget-object v8, Landroidx/work/impl/model/r;->x:Ljava/lang/String;
    const-string v9, "Interval duration lesser than minimum allowed value; Changed to 900000"
    if-gez v0, :cond_46a
    invoke-static {}, Landroidx/work/y;->d()Landroidx/work/y;
    move-result-object v10
    invoke-virtual {v10, v8, v9}, Landroidx/work/y;->g(Ljava/lang/String;Ljava/lang/String;)V
    :cond_46a
    if-gez v0, :cond_46e
    move-wide v10, v6
    goto :goto_46f
    :cond_46e
    move-wide v10, v4
    :goto_46f
    if-gez v0, :cond_473
    move-wide v12, v6
    goto :goto_474
    :cond_473
    move-wide v12, v4
    :goto_474
    cmp-long v0, v10, v6
    if-gez v0, :cond_47f
    invoke-static {}, Landroidx/work/y;->d()Landroidx/work/y;
    move-result-object v4
    invoke-virtual {v4, v8, v9}, Landroidx/work/y;->g(Ljava/lang/String;Ljava/lang/String;)V
    :cond_47f
    if-gez v0, :cond_482
    goto :goto_483
    :cond_482
    move-wide v6, v10
    :goto_483
    iput-wide v6, v3, Landroidx/work/impl/model/r;->h:J
    const-wide/32 v4, 0x493e0
    cmp-long v0, v12, v4
    if-gez v0, :cond_495
    invoke-static {}, Landroidx/work/y;->d()Landroidx/work/y;
    move-result-object v0
    const-string v4, "Flex duration lesser than minimum allowed value; Changed to 300000"
    invoke-virtual {v0, v8, v4}, Landroidx/work/y;->g(Ljava/lang/String;Ljava/lang/String;)V
    :cond_495
    iget-wide v4, v3, Landroidx/work/impl/model/r;->h:J
    cmp-long v0, v12, v4
    if-lez v0, :cond_4b0
    invoke-static {}, Landroidx/work/y;->d()Landroidx/work/y;
    move-result-object v0
    new-instance v4, Ljava/lang/StringBuilder;
    const-string v5, "Flex duration greater than interval duration; Changed to "
    invoke-direct {v4, v5}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V
    invoke-virtual {v4, v10, v11}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;
    invoke-virtual {v4}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;
    move-result-object v4
    invoke-virtual {v0, v8, v4}, Landroidx/work/y;->g(Ljava/lang/String;Ljava/lang/String;)V
    :cond_4b0
    const-wide/32 v14, 0x493e0
    iget-wide v4, v3, Landroidx/work/impl/model/r;->h:J
    move-wide/from16 v16, v4
    invoke-static/range {v12 .. v17}, Lcom/bumptech/glide/c;->m(JJJ)J
    move-result-wide v4
    iput-wide v4, v3, Landroidx/work/impl/model/r;->i:J
    invoke-virtual {v2}, Landroidx/work/p0;->a()Landroidx/work/q0;
    move-result-object v0
    check-cast v0, Landroidx/work/j0;
    invoke-virtual {v1}, Landroid/content/Context;->getApplicationContext()Landroid/content/Context;
    move-result-object v2
    invoke-static {v2}, Landroidx/work/impl/t;->z(Landroid/content/Context;)Landroidx/work/impl/t;
    move-result-object v2
    sget-object v3, Landroidx/work/l;->KEEP:Landroidx/work/l;
    invoke-virtual {v2}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    sget-object v4, Landroidx/work/l;->UPDATE:Landroidx/work/l;
    const-string v5, "alarm_reconcile_daily"
    if-ne v3, v4, :cond_4da
    invoke-static {v2, v0}, Llc/a;->o(Landroidx/work/impl/t;Landroidx/work/j0;)Landroidx/work/impl/e;
    goto :goto_4e8
    :cond_4da
    sget-object v3, Landroidx/work/m;->KEEP:Landroidx/work/m;
    new-instance v4, Landroidx/work/impl/n;
    invoke-static {v0}, Ljava/util/Collections;->singletonList(Ljava/lang/Object;)Ljava/util/List;
    move-result-object v0
    invoke-direct {v4, v2, v5, v3, v0}, Landroidx/work/impl/n;-><init>(Landroidx/work/impl/t;Ljava/lang/String;Landroidx/work/m;Ljava/util/List;)V
    invoke-virtual {v4}, Landroidx/work/impl/n;->O()Landroidx/work/g0;
    :goto_4e8
    return-void
    :goto_4e9
    :try_start_4e9
    monitor-exit v8
    :try_end_4ea
    .catchall {:try_start_4e9 .. :try_end_4ea} :catchall_396
    :try_start_4ea
    throw v0
    :catchall_4eb
    move-exception v0
    monitor-exit v4
    :try_end_4ed
    .catchall {:try_start_4ea .. :try_end_4ed} :catchall_4eb
    throw v0
