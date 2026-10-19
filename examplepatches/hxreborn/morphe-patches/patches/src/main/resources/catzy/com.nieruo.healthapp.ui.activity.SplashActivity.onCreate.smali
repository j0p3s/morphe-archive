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
    sget-object p1, Lcom/orhanobut/hawk/c;->a:Lcom/orhanobut/hawk/h;
    invoke-interface {p1}, Lcom/orhanobut/hawk/h;->e()Z
    move-result p1
    if-nez p1, :cond_9a
    invoke-static {p0}, Ln0/d;->y(Landroid/content/Context;)V
    :cond_9a
    sget-object p1, Lcom/nieruo/healthapp/manager/a1;->m:Lfe/d;
    invoke-virtual {p1}, Lfe/d;->t()Lcom/nieruo/healthapp/manager/a1;
    move-result-object p1
    invoke-virtual {p1}, Lcom/nieruo/healthapp/manager/a1;->b()Z
    move-result p1
    iput-boolean p1, p0, Lcom/nieruo/healthapp/ui/activity/SplashActivity;->b:Z
    invoke-static {p0}, Landroidx/lifecycle/i1;->d(Landroidx/lifecycle/e0;)Landroidx/lifecycle/y;
    move-result-object p1
    new-instance v1, Lcom/nieruo/healthapp/ui/activity/kp;
    const/4 v2, 0x0
    invoke-direct {v1, p0, v2}, Lcom/nieruo/healthapp/ui/activity/kp;-><init>(Lcom/nieruo/healthapp/ui/activity/SplashActivity;Lkotlin/coroutines/d;)V
    const/4 v3, 0x3
    invoke-static {p1, v2, v2, v1, v3}, Lkotlinx/coroutines/j0;->n(Lkotlinx/coroutines/f0;Lkotlin/coroutines/j;Lkotlinx/coroutines/h0;Lkotlin/jvm/functions/Function2;I)Lkotlinx/coroutines/e2;
    new-instance p1, Lcom/nieruo/healthapp/ui/activity/jp;
    const/4 v1, 0x0
    invoke-direct {p1, v1}, Lcom/nieruo/healthapp/ui/activity/jp;-><init>(I)V
    const/16 v1, 0xa
    invoke-virtual {v0, p1}, Landroidx/work/impl/model/c;->I0(Lx0/g;)V
    invoke-static {p0}, Landroidx/lifecycle/i1;->d(Landroidx/lifecycle/e0;)Landroidx/lifecycle/y;
    move-result-object p1
    new-instance v0, Lcom/nieruo/healthapp/ui/activity/lp;
    invoke-direct {v0, p0, v2}, Lcom/nieruo/healthapp/ui/activity/lp;-><init>(Lcom/nieruo/healthapp/ui/activity/SplashActivity;Lkotlin/coroutines/d;)V
    invoke-static {p1, v2, v2, v0, v3}, Lkotlinx/coroutines/j0;->n(Lkotlinx/coroutines/f0;Lkotlin/coroutines/j;Lkotlinx/coroutines/h0;Lkotlin/jvm/functions/Function2;I)Lkotlinx/coroutines/e2;
    return-void
