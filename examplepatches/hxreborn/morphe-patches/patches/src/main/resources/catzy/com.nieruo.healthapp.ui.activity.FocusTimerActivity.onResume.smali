    invoke-super {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->onResume()V
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->y:Lkotlinx/coroutines/e2;
    const/4 v1, 0x0
    if-eqz v0, :cond_b
    invoke-virtual {v0, v1}, Lkotlinx/coroutines/w1;->b(Ljava/util/concurrent/CancellationException;)V
    :cond_b
    :try_start_b
    invoke-static {}, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->u0()Lcom/nieruo/healthapp/resource/entry/FocusThemesEntry;
    move-result-object v0
    invoke-static {v0}, Lik/q;->constructor-impl(Ljava/lang/Object;)Ljava/lang/Object;
    move-result-object v0
    :try_end_13
    .catchall {:try_start_b .. :try_end_13} :catchall_14
    goto :goto_19
    :catchall_14
    move-exception v0
    invoke-static {v0}, La0/a;->f(Ljava/lang/Throwable;)Ljava/lang/Object;
    move-result-object v0
    :goto_19
    invoke-static {v0}, Lik/q;->isFailure-impl(Ljava/lang/Object;)Z
    move-result v2
    if-eqz v2, :cond_20
    move-object v0, v1
    :cond_20
    check-cast v0, Lcom/nieruo/healthapp/resource/entry/FocusThemesEntry;
    if-nez v0, :cond_25
    goto :goto_58
    :cond_25
    invoke-virtual {v0}, Lcom/nieruo/healthapp/resource/entry/FocusThemesEntry;->t()I
    move-result v2
    iget-object v3, p0, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->x:Lcom/nieruo/healthapp/resource/entry/FocusThemesEntry;
    invoke-virtual {v3}, Lcom/nieruo/healthapp/resource/entry/FocusThemesEntry;->t()I
    move-result v3
    const/4 v4, 0x3
    if-ne v2, v3, :cond_49
    invoke-virtual {v0}, Lcom/nieruo/healthapp/resource/entry/FocusThemesEntry;->D()Z
    move-result v2
    if-eqz v2, :cond_39
    goto :goto_58
    :cond_39
    invoke-static {p0}, Landroidx/lifecycle/i1;->d(Landroidx/lifecycle/e0;)Landroidx/lifecycle/y;
    move-result-object v2
    new-instance v3, Lcom/nieruo/healthapp/ui/activity/p7;
    invoke-direct {v3, v0, p0, v1}, Lcom/nieruo/healthapp/ui/activity/p7;-><init>(Lcom/nieruo/healthapp/resource/entry/FocusThemesEntry;Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;Lkotlin/coroutines/d;)V
    invoke-static {v2, v1, v1, v3, v4}, Lkotlinx/coroutines/j0;->n(Lkotlinx/coroutines/f0;Lkotlin/coroutines/j;Lkotlinx/coroutines/h0;Lkotlin/jvm/functions/Function2;I)Lkotlinx/coroutines/e2;
    move-result-object v0
    iput-object v0, p0, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->y:Lkotlinx/coroutines/e2;
    goto :goto_58
    :cond_49
    invoke-static {p0}, Landroidx/lifecycle/i1;->d(Landroidx/lifecycle/e0;)Landroidx/lifecycle/y;
    move-result-object v2
    new-instance v3, Lcom/nieruo/healthapp/ui/activity/q7;
    invoke-direct {v3, v0, p0, v1}, Lcom/nieruo/healthapp/ui/activity/q7;-><init>(Lcom/nieruo/healthapp/resource/entry/FocusThemesEntry;Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;Lkotlin/coroutines/d;)V
    invoke-static {v2, v1, v1, v3, v4}, Lkotlinx/coroutines/j0;->n(Lkotlinx/coroutines/f0;Lkotlin/coroutines/j;Lkotlinx/coroutines/h0;Lkotlin/jvm/functions/Function2;I)Lkotlinx/coroutines/e2;
    move-result-object v0
    iput-object v0, p0, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->y:Lkotlinx/coroutines/e2;
    :goto_58
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->s0()V
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->r0()V
    invoke-virtual {p0}, Landroid/content/Context;->getContentResolver()Landroid/content/ContentResolver;
    move-result-object v0
    const-string v1, "accelerometer_rotation"
    invoke-static {v1}, Landroid/provider/Settings$System;->getUriFor(Ljava/lang/String;)Landroid/net/Uri;
    move-result-object v1
    const/4 v2, 0x0
    iget-object v3, p0, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->e:Lcom/google/android/gms/internal/measurement/w3;
    invoke-virtual {v0, v1, v2, v3}, Landroid/content/ContentResolver;->registerContentObserver(Landroid/net/Uri;ZLandroid/database/ContentObserver;)V
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->B0()V
    invoke-virtual {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->getBinding()Lx4/a;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/databinding/ActivityFocusTimerBinding;
    iget-object v0, v0, Lcom/nieruo/healthapp/databinding/ActivityFocusTimerBinding;->i:Lcom/google/android/material/imageview/ShapeableImageView;
    sget-object v1, Lsi/c;->a:Ljava/util/ArrayList;
    invoke-static {}, Lsi/c;->d()Z
    move-result v1
    invoke-virtual {v0, v1}, Landroid/view/View;->setSelected(Z)V
    invoke-virtual {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->getBinding()Lx4/a;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/databinding/ActivityFocusTimerBinding;
    iget-object v0, v0, Lcom/nieruo/healthapp/databinding/ActivityFocusTimerBinding;->f:Landroid/widget/ImageView;
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->n0()Z
    move-result v1
    invoke-virtual {v0, v1}, Landroid/widget/ImageView;->setSelected(Z)V
    return-void
