    invoke-super {p0}, Lcom/nieruo/healthapp/ui/activity/BaseVipActivity;->onResume()V
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/BaseVipActivity;->getPurchaseEntity()Lcom/nieruo/healthapp/entity/PurchaseEntity;
    move-result-object v0
    const/4 v1, 0x0
    if-eqz v0, :cond_29
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/BaseVipActivity;->getVipViewModel()Lcom/nieruo/healthapp/viewmodel/w2;
    move-result-object v2
    iput v1, v2, Lcom/nieruo/healthapp/viewmodel/w2;->g:I
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/BaseVipActivity;->getVipViewModel()Lcom/nieruo/healthapp/viewmodel/w2;
    move-result-object v2
    iput-boolean v1, v2, Lcom/nieruo/healthapp/viewmodel/w2;->h:Z
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/BaseVipActivity;->getVipViewModel()Lcom/nieruo/healthapp/viewmodel/w2;
    move-result-object v2
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/PurchaseEntity;->getUid()I
    move-result v3
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/PurchaseEntity;->getOrder_id()Ljava/lang/String;
    move-result-object v4
    invoke-virtual {v0}, Lcom/nieruo/healthapp/entity/PurchaseEntity;->getPurchaseToken()Ljava/lang/String;
    move-result-object v0
    invoke-virtual {v2, v3, v4, v0}, Lcom/nieruo/healthapp/viewmodel/w2;->i(ILjava/lang/String;Ljava/lang/String;)V
    :cond_29
    sget-boolean v0, Lcom/nieruo/healthapp/ui/activity/HomeActivity;->Z:Z
    const/4 v2, 0x0
    if-nez v0, :cond_4d
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/HomeActivity;->getHomeViewModel()Lcom/nieruo/healthapp/viewmodel/m0;
    move-result-object v0
    invoke-virtual {v0}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    sget-object v3, Lcom/nieruo/healthapp/manager/a1;->m:Lfe/d;
    invoke-virtual {v3}, Lfe/d;->t()Lcom/nieruo/healthapp/manager/a1;
    move-result-object v3
    invoke-virtual {v3}, Lcom/nieruo/healthapp/manager/a1;->r()Ljava/lang/Integer;
    move-result-object v3
    if-eqz v3, :cond_4d
    invoke-virtual {v3}, Ljava/lang/Number;->intValue()I
    move-result v3
    new-instance v4, Lcom/nieruo/healthapp/viewmodel/i0;
    invoke-direct {v4, v3, v0, v2}, Lcom/nieruo/healthapp/viewmodel/i0;-><init>(ILcom/nieruo/healthapp/viewmodel/m0;Lkotlin/coroutines/d;)V
    invoke-virtual {v0, v4}, Lcom/nieruo/healthapp/viewmodel/e;->e(Lkotlin/jvm/functions/Function1;)V
    :cond_4d
    sput-boolean v1, Lcom/nieruo/healthapp/ui/activity/HomeActivity;->Z:Z
    sget-object v0, Lcom/nieruo/healthapp/realm/dao/u1;->b:Lcom/nieruo/healthapp/realm/dao/g;
    invoke-virtual {v0}, Lcom/nieruo/healthapp/realm/dao/g;->r()Lcom/nieruo/healthapp/realm/dao/u1;
    move-result-object v0
    invoke-virtual {v0}, Lcom/nieruo/healthapp/realm/dao/u1;->i()Z
    move-result v0
    if-eqz v0, :cond_68
    invoke-static {p0}, Landroidx/lifecycle/i1;->d(Landroidx/lifecycle/e0;)Landroidx/lifecycle/y;
    move-result-object v0
    new-instance v3, Lcom/nieruo/healthapp/ui/activity/wc;
    invoke-direct {v3, p0, v2}, Lcom/nieruo/healthapp/ui/activity/wc;-><init>(Lcom/nieruo/healthapp/ui/activity/HomeActivity;Lkotlin/coroutines/d;)V
    invoke-static {v0, v3}, Lqd/z1;->D(Lkotlinx/coroutines/f0;Lkotlin/jvm/functions/Function2;)Lkotlinx/coroutines/e2;
    goto :goto_6b
    :cond_68
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/HomeActivity;->O0()V
    :goto_6b
    sget-boolean v0, Lcom/nieruo/healthapp/ui/activity/HomeActivity;->b0:Z
    if-nez v0, :cond_10a
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/HomeActivity;->z0()Ljava/lang/Boolean;
    move-result-object v0
    sget-object v3, Ljava/lang/Boolean;->TRUE:Ljava/lang/Boolean;
    invoke-static {v0, v3}, Lkotlin/jvm/internal/Intrinsics;->areEqual(Ljava/lang/Object;Ljava/lang/Object;)Z
    move-result v0
    if-nez v0, :cond_7d
    goto/16 :goto_10a
    :cond_7d
    sget-boolean v0, Lcom/nieruo/healthapp/ui/activity/HomeActivity;->d0:Z
    if-eqz v0, :cond_83
    goto/16 :goto_10c
    :cond_83
    sget-object v0, Lcom/nieruo/healthapp/realm/dao/g2;->e:Lcom/nieruo/healthapp/realm/dao/g;
    invoke-virtual {v0}, Lcom/nieruo/healthapp/realm/dao/g;->s()Lcom/nieruo/healthapp/realm/dao/g2;
    move-result-object v0
    const/4 v3, 0x1
    invoke-virtual {v0, v3}, Lcom/nieruo/healthapp/realm/dao/g2;->x(Z)Lcom/nieruo/healthapp/realm/entity/User;
    move-result-object v0
    if-eqz v0, :cond_95
    invoke-virtual {v0}, Lcom/nieruo/healthapp/realm/entity/User;->l()Z
    move-result v0
    goto :goto_96
    :cond_95
    move v0, v1
    :goto_96
    sget-object v4, Lcom/nieruo/healthapp/realm/dao/n;->a:Lcom/nieruo/healthapp/realm/dao/m;
    invoke-virtual {v4}, Lcom/nieruo/healthapp/realm/dao/m;->a()Lcom/nieruo/healthapp/realm/dao/n;
    invoke-static {}, Lcom/nieruo/healthapp/realm/dao/n;->j()Lcom/nieruo/healthapp/realm/entity/CatlyDay;
    move-result-object v5
    if-eqz v5, :cond_a5
    invoke-virtual {v5}, Lcom/nieruo/healthapp/realm/entity/CatlyDay;->o()Z
    move-result v1
    :cond_a5
    invoke-static {}, Lcom/nieruo/healthapp/utils/a1;->g()Lkotlinx/datetime/w;
    move-result-object v5
    invoke-virtual {v4}, Lcom/nieruo/healthapp/realm/dao/m;->a()Lcom/nieruo/healthapp/realm/dao/n;
    invoke-static {}, Lcom/nieruo/healthapp/realm/dao/n;->h()Lcom/nieruo/healthapp/realm/entity/CatlyDay;
    move-result-object v6
    if-eqz v6, :cond_be
    invoke-virtual {v6}, Lcom/nieruo/healthapp/realm/entity/CatlyDay;->q()Lgk/h;
    move-result-object v6
    if-eqz v6, :cond_be
    invoke-static {v6}, Lcom/nieruo/healthapp/realm/i;->e(Lgk/h;)Lkotlinx/datetime/w;
    move-result-object v6
    if-nez v6, :cond_bf
    :cond_be
    move-object v6, v5
    :cond_bf
    sget-object v7, Lkotlinx/datetime/i0;->Companion:Lkotlinx/datetime/h0;
    invoke-virtual {v7}, Ljava/lang/Object;->getClass()Ljava/lang/Class;
    sget-object v7, Lkotlinx/datetime/i0;->b:Lkotlinx/datetime/t;
    invoke-static {v6, v5, v7}, Landroidx/work/o0;->c(Lkotlinx/datetime/w;Lkotlinx/datetime/w;Lkotlinx/datetime/t;)I
    move-result v5
    const-class v6, Lcom/nieruo/healthapp/ui/activity/SevenDayTalkGuideActivity;
    if-gtz v5, :cond_df
    sget-object v0, Lcom/nieruo/healthapp/utils/t0;->a:Lcom/nieruo/healthapp/utils/t0;
    invoke-virtual {v0}, Lcom/nieruo/healthapp/utils/t0;->b()Z
    move-result v0
    if-eqz v0, :cond_10c
    new-instance v0, Landroid/content/Intent;
    invoke-direct {v0, p0, v6}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
    invoke-virtual {p0, v0}, Landroid/app/Activity;->startActivity(Landroid/content/Intent;)V
    goto :goto_10c
    :cond_df
    if-nez v0, :cond_f2
    if-nez v1, :cond_f2
    sput-boolean v3, Lcom/nieruo/healthapp/ui/activity/HomeActivity;->c0:Z
    sput-boolean v3, Lcom/nieruo/healthapp/ui/activity/HomeActivity;->W:Z
    new-instance v0, Landroid/content/Intent;
    const-class v1, Lcom/nieruo/healthapp/ui/activity/DayTogetherActivity;
    invoke-direct {v0, p0, v1}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
    invoke-virtual {p0, v0}, Landroid/app/Activity;->startActivity(Landroid/content/Intent;)V
    goto :goto_10c
    :cond_f2
    invoke-virtual {v4}, Lcom/nieruo/healthapp/realm/dao/m;->a()Lcom/nieruo/healthapp/realm/dao/n;
    move-result-object v0
    invoke-static {v0}, Lcom/nieruo/healthapp/realm/dao/n;->f(Lcom/nieruo/healthapp/realm/dao/n;)Lcom/nieruo/healthapp/realm/entity/CatlyDay;
    sget-object v0, Lcom/nieruo/healthapp/utils/t0;->a:Lcom/nieruo/healthapp/utils/t0;
    invoke-virtual {v0}, Lcom/nieruo/healthapp/utils/t0;->b()Z
    move-result v0
    if-eqz v0, :cond_10c
    new-instance v0, Landroid/content/Intent;
    invoke-direct {v0, p0, v6}, Landroid/content/Intent;-><init>(Landroid/content/Context;Ljava/lang/Class;)V
    invoke-virtual {p0, v0}, Landroid/app/Activity;->startActivity(Landroid/content/Intent;)V
    goto :goto_10c
    :cond_10a
    :goto_10a
    sput-boolean v1, Lcom/nieruo/healthapp/ui/activity/HomeActivity;->b0:Z
    :cond_10c
    :goto_10c
    sget-boolean v0, Lcom/nieruo/healthapp/ui/activity/HomeActivity;->W:Z
    if-eqz v0, :cond_111
    return-void
    :cond_111
    invoke-static {p0}, Landroidx/lifecycle/i1;->d(Landroidx/lifecycle/e0;)Landroidx/lifecycle/y;
    move-result-object v0
    new-instance v1, Lcom/nieruo/healthapp/ui/activity/ic;
    invoke-direct {v1, p0, v2}, Lcom/nieruo/healthapp/ui/activity/ic;-><init>(Lcom/nieruo/healthapp/ui/activity/HomeActivity;Lkotlin/coroutines/d;)V
    const/4 v3, 0x3
    invoke-static {v0, v2, v2, v1, v3}, Lkotlinx/coroutines/j0;->n(Lkotlinx/coroutines/f0;Lkotlin/coroutines/j;Lkotlinx/coroutines/h0;Lkotlin/jvm/functions/Function2;I)Lkotlinx/coroutines/e2;
    return-void
