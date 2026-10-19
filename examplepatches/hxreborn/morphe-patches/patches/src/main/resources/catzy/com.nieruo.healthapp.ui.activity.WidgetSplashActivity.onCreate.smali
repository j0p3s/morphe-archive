    sget v0, Landroid/os/Build$VERSION;->SDK_INT:I
    const/16 v1, 0x1f
    if-lt v0, v1, :cond_c
    new-instance v0, Lx0/f;
    invoke-direct {v0, p0}, Lx0/f;-><init>(Li/l;)V
    goto :goto_11
    :cond_c
    new-instance v0, Landroidx/work/impl/model/c;
    invoke-direct {v0, p0}, Landroidx/work/impl/model/c;-><init>(Li/l;)V
    :goto_11
    invoke-virtual {v0}, Landroidx/work/impl/model/c;->C0()V
    new-instance v1, Landroidx/activity/v0;
    sget-object v2, Landroidx/activity/u0;->INSTANCE:Landroidx/activity/u0;
    const/4 v3, 0x0
    const/4 v4, 0x1
    invoke-direct {v1, v3, v3, v4, v2}, Landroidx/activity/v0;-><init>(IIILkotlin/jvm/functions/Function1;)V
    invoke-static {p0, v1}, Landroidx/activity/w;->a(Li/l;Landroidx/activity/v0;)V
    invoke-virtual {p0}, Landroid/app/Activity;->getWindow()Landroid/view/Window;
    move-result-object v1
    invoke-virtual {p0}, Landroid/app/Activity;->getWindow()Landroid/view/Window;
    move-result-object v2
    invoke-virtual {v2}, Landroid/view/Window;->getDecorView()Landroid/view/View;
    move-result-object v2
    new-instance v5, Lcn/thinkingdata/analytics/tasks/b;
    invoke-direct {v5, v2}, Lcn/thinkingdata/analytics/tasks/b;-><init>(Landroid/view/View;)V
    sget v2, Landroid/os/Build$VERSION;->SDK_INT:I
    const/16 v6, 0x23
    if-lt v2, v6, :cond_3d
    new-instance v2, Landroidx/core/view/r2;
    invoke-direct {v2, v1, v5}, Landroidx/core/view/r2;-><init>(Landroid/view/Window;Lcn/thinkingdata/analytics/tasks/b;)V
    goto :goto_56
    :cond_3d
    const/16 v7, 0x1e
    if-lt v2, v7, :cond_47
    new-instance v2, Landroidx/core/view/q2;
    invoke-direct {v2, v1, v5}, Landroidx/core/view/q2;-><init>(Landroid/view/Window;Lcn/thinkingdata/analytics/tasks/b;)V
    goto :goto_56
    :cond_47
    const/16 v7, 0x1a
    if-lt v2, v7, :cond_51
    new-instance v2, Landroidx/core/view/p2;
    invoke-direct {v2, v1, v5}, Landroidx/core/view/p2;-><init>(Landroid/view/Window;Lcn/thinkingdata/analytics/tasks/b;)V
    goto :goto_56
    :cond_51
    new-instance v2, Landroidx/core/view/o2;
    invoke-direct {v2, v1, v5}, Landroidx/core/view/o2;-><init>(Landroid/view/Window;Lcn/thinkingdata/analytics/tasks/b;)V
    :goto_56
    invoke-virtual {v2, v4}, Lqd/z1;->N(Z)V
    invoke-virtual {v2, v4}, Lqd/z1;->M(Z)V
    invoke-static {p0}, Lk8/d;->q(Li/l;)V
    invoke-super {p0, p1}, Landroidx/fragment/app/i0;->onCreate(Landroid/os/Bundle;)V
    sget p1, Landroid/os/Build$VERSION;->SDK_INT:I
    const/16 v1, 0x1d
    if-lt p1, v1, :cond_76
    invoke-virtual {p0}, Landroid/app/Activity;->getWindow()Landroid/view/Window;
    move-result-object v1
    invoke-static {v1}, La1/e;->x(Landroid/view/Window;)V
    invoke-virtual {p0}, Landroid/app/Activity;->getWindow()Landroid/view/Window;
    move-result-object v1
    invoke-static {v1}, La1/e;->l(Landroid/view/Window;)V
    :cond_76
    invoke-virtual {p0}, Landroid/app/Activity;->getWindow()Landroid/view/Window;
    move-result-object v1
    const/high16 v2, -0x80000000
    invoke-virtual {v1, v2}, Landroid/view/Window;->addFlags(I)V
    if-ge p1, v6, :cond_8f
    invoke-virtual {p0}, Landroid/app/Activity;->getWindow()Landroid/view/Window;
    move-result-object p1
    invoke-virtual {p1, v3}, Landroid/view/Window;->setNavigationBarColor(I)V
    invoke-virtual {p0}, Landroid/app/Activity;->getWindow()Landroid/view/Window;
    move-result-object p1
    invoke-virtual {p1, v3}, Landroid/view/Window;->setStatusBarColor(I)V
    :cond_8f
    sget-object p1, Lcom/nieruo/healthapp/manager/a1;->m:Lfe/d;
    invoke-virtual {p1}, Lfe/d;->t()Lcom/nieruo/healthapp/manager/a1;
    move-result-object p1
    invoke-virtual {p1}, Lcom/nieruo/healthapp/manager/a1;->b()Z
    move-result p1
    iput-boolean p1, p0, Lcom/nieruo/healthapp/ui/activity/WidgetSplashActivity;->b:Z
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v1, "key_widget_entity_id"
    const-wide/16 v5, 0x0
    invoke-virtual {p1, v1, v5, v6}, Landroid/content/Intent;->getLongExtra(Ljava/lang/String;J)J
    move-result-wide v1
    sget-object p1, Lcom/nieruo/healthapp/manager/s1;->n:Lkc/f;
    invoke-virtual {p1}, Lkc/f;->M()Lcom/nieruo/healthapp/manager/s1;
    move-result-object p1
    invoke-virtual {p1, v1, v2}, Lcom/nieruo/healthapp/manager/s1;->n(J)Lcom/nieruo/healthapp/entity/WidgetDataEntity;
    move-result-object v8
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v1, "key_widget_type"
    const/4 v2, -0x1
    invoke-virtual {p1, v1, v2}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I
    move-result v9
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object p1
    const-string v1, "key_widget_id"
    invoke-virtual {p1, v1, v3}, Landroid/content/Intent;->getIntExtra(Ljava/lang/String;I)I
    move-result v10
    sget p1, Lcom/nieruo/healthapp/utils/f;->a:I
    const/4 p1, 0x0
    if-eqz v8, :cond_d0
    invoke-virtual {v8, p0, v4}, Lcom/nieruo/healthapp/entity/WidgetDataEntity;->getName(Landroid/content/Context;Z)Ljava/lang/String;
    move-result-object v1
    goto :goto_d1
    :cond_d0
    move-object v1, p1
    :goto_d1
    const-string v2, "\u5c0f\u7ec4\u4ef6"
    sput-object v2, Lcom/nieruo/healthapp/utils/f;->d:Ljava/lang/String;
    sput-object v1, Lcom/nieruo/healthapp/utils/f;->e:Ljava/lang/String;
    const-string v1, "\u70b9\u51fb"
    packed-switch v9, :pswitch_data_134
    goto :goto_ee
    :pswitch_dd
    const-string v2, "\u5927\u53f7"
    invoke-static {v1, v2, v8, p1}, Landroidx/work/impl/v;->V0(Ljava/lang/String;Ljava/lang/String;Lcom/nieruo/healthapp/entity/WidgetDataEntity;Landroid/content/Context;)V
    goto :goto_ee
    :pswitch_e3
    const-string v2, "\u4e2d\u53f7-\u6a2a"
    invoke-static {v1, v2, v8, p1}, Landroidx/work/impl/v;->V0(Ljava/lang/String;Ljava/lang/String;Lcom/nieruo/healthapp/entity/WidgetDataEntity;Landroid/content/Context;)V
    goto :goto_ee
    :pswitch_e9
    const-string v2, "\u5c0f\u53f7"
    invoke-static {v1, v2, v8, p1}, Landroidx/work/impl/v;->V0(Ljava/lang/String;Ljava/lang/String;Lcom/nieruo/healthapp/entity/WidgetDataEntity;Landroid/content/Context;)V
    :goto_ee
    sget-object v1, Leh/a;->b:Leh/d;
    invoke-virtual {v1}, Leh/d;->a()Leh/a;
    move-result-object v2
    const-class v5, Lcom/nieruo/healthapp/ui/activity/HomeActivity;
    invoke-virtual {v2, v5}, Leh/a;->e(Ljava/lang/Class;)Landroid/app/Activity;
    move-result-object v2
    if-nez v2, :cond_10a
    invoke-virtual {v1}, Leh/d;->a()Leh/a;
    move-result-object v1
    const-class v2, Lcom/nieruo/healthapp/ui/activity/WelcomeActivity;
    invoke-virtual {v1, v2}, Leh/a;->e(Ljava/lang/Class;)Landroid/app/Activity;
    move-result-object v1
    if-nez v1, :cond_10a
    move v6, v4
    goto :goto_10b
    :cond_10a
    move v6, v3
    :goto_10b
    const/4 v1, 0x3
    if-eqz v6, :cond_11a
    invoke-static {p0}, Landroidx/lifecycle/i1;->d(Landroidx/lifecycle/e0;)Landroidx/lifecycle/y;
    move-result-object v2
    new-instance v3, Lcom/nieruo/healthapp/ui/activity/eu;
    invoke-direct {v3, p0, p1}, Lcom/nieruo/healthapp/ui/activity/eu;-><init>(Lcom/nieruo/healthapp/ui/activity/WidgetSplashActivity;Lkotlin/coroutines/d;)V
    invoke-static {v2, p1, p1, v3, v1}, Lkotlinx/coroutines/j0;->n(Lkotlinx/coroutines/f0;Lkotlin/coroutines/j;Lkotlinx/coroutines/h0;Lkotlin/jvm/functions/Function2;I)Lkotlinx/coroutines/e2;
    :cond_11a
    new-instance v2, Landroidx/core/view/h;
    const/16 v3, 0x9
    invoke-direct {v2, v3}, Landroidx/core/view/h;-><init>(I)V
    invoke-virtual {v0, v2}, Landroidx/work/impl/model/c;->I0(Lx0/g;)V
    invoke-static {p0}, Landroidx/lifecycle/i1;->d(Landroidx/lifecycle/e0;)Landroidx/lifecycle/y;
    move-result-object v0
    new-instance v5, Lcom/nieruo/healthapp/ui/activity/fu;
    const/4 v11, 0x0
    move-object v7, p0
    invoke-direct/range {v5 .. v11}, Lcom/nieruo/healthapp/ui/activity/fu;-><init>(ZLcom/nieruo/healthapp/ui/activity/WidgetSplashActivity;Lcom/nieruo/healthapp/entity/WidgetDataEntity;IILkotlin/coroutines/d;)V
    invoke-static {v0, p1, p1, v5, v1}, Lkotlinx/coroutines/j0;->n(Lkotlinx/coroutines/f0;Lkotlin/coroutines/j;Lkotlinx/coroutines/h0;Lkotlin/jvm/functions/Function2;I)Lkotlinx/coroutines/e2;
    return-void
    nop
    :pswitch_data_134
    .packed-switch 0x3000
        :pswitch_e9
        :pswitch_e3
        :pswitch_dd
    .end packed-switch
