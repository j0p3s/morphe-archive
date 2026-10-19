    invoke-super {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->onResume()V
    sget-object v0, Lcom/nieruo/healthapp/manager/a1;->m:Lfe/d;
    invoke-virtual {v0}, Lfe/d;->t()Lcom/nieruo/healthapp/manager/a1;
    move-result-object v1
    invoke-virtual {v1}, Lcom/nieruo/healthapp/manager/a1;->D()Z
    move-result v1
    iget-boolean v2, p0, Lcom/nieruo/healthapp/ui/activity/FocusHomeActivity;->h:Z
    if-eqz v2, :cond_1a
    if-nez v1, :cond_1a
    invoke-static {}, La8/q;->p()Lcom/nieruo/healthapp/resource/entry/FocusThemesEntry;
    move-result-object v2
    invoke-virtual {p0, v2}, Lcom/nieruo/healthapp/ui/activity/FocusHomeActivity;->o0(Lcom/nieruo/healthapp/resource/entry/FocusThemesEntry;)V
    :cond_1a
    iput-boolean v1, p0, Lcom/nieruo/healthapp/ui/activity/FocusHomeActivity;->h:Z
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/FocusHomeActivity;->u0()V
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v1
    const-string v2, "key_reset_focus_home_app_bar"
    const/4 v3, 0x0
    invoke-virtual {v1, v2, v3}, Landroid/content/Intent;->getBooleanExtra(Ljava/lang/String;Z)Z
    move-result v1
    if-eqz v1, :cond_44
    invoke-virtual {p0}, Landroid/app/Activity;->getIntent()Landroid/content/Intent;
    move-result-object v1
    invoke-virtual {v1, v2}, Landroid/content/Intent;->removeExtra(Ljava/lang/String;)V
    invoke-virtual {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->getBinding()Lx4/a;
    move-result-object v1
    check-cast v1, Lcom/nieruo/healthapp/databinding/ActivityFocusHomeBinding;
    iget-object v1, v1, Lcom/nieruo/healthapp/databinding/ActivityFocusHomeBinding;->b:Lcom/google/android/material/appbar/AppBarLayout;
    new-instance v2, Lcom/nieruo/healthapp/ui/activity/q5;
    const/4 v4, 0x0
    invoke-direct {v2, p0, v4}, Lcom/nieruo/healthapp/ui/activity/q5;-><init>(Lcom/nieruo/healthapp/ui/activity/FocusHomeActivity;I)V
    invoke-virtual {v1, v2}, Landroid/view/View;->post(Ljava/lang/Runnable;)Z
    :cond_44
    invoke-virtual {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->getBinding()Lx4/a;
    move-result-object v1
    check-cast v1, Lcom/nieruo/healthapp/databinding/ActivityFocusHomeBinding;
    iget-object v1, v1, Lcom/nieruo/healthapp/databinding/ActivityFocusHomeBinding;->i:Landroidx/constraintlayout/widget/ConstraintLayout;
    invoke-virtual {v1}, Landroid/view/View;->isClickable()Z
    move-result v1
    if-nez v1, :cond_9e
    invoke-virtual {v0}, Lfe/d;->t()Lcom/nieruo/healthapp/manager/a1;
    move-result-object v0
    invoke-virtual {v0}, Lcom/nieruo/healthapp/manager/a1;->D()Z
    move-result v0
    if-eqz v0, :cond_6a
    invoke-virtual {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->getBinding()Lx4/a;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/databinding/ActivityFocusHomeBinding;
    iget-object v0, v0, Lcom/nieruo/healthapp/databinding/ActivityFocusHomeBinding;->i:Landroidx/constraintlayout/widget/ConstraintLayout;
    const/16 v1, 0x8
    invoke-virtual {v0, v1}, Landroid/view/View;->setVisibility(I)V
    return-void
    :cond_6a
    invoke-virtual {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->getBinding()Lx4/a;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/databinding/ActivityFocusHomeBinding;
    iget-object v0, v0, Lcom/nieruo/healthapp/databinding/ActivityFocusHomeBinding;->z:Lcom/airbnb/lottie/LottieAnimationView;
    invoke-virtual {v0}, Lcom/airbnb/lottie/LottieAnimationView;->getComposition()Lr5/l;
    move-result-object v1
    if-eqz v1, :cond_9e
    iget v1, v1, Lr5/l;->m:F
    float-to-int v1, v1
    invoke-virtual {v0}, Lcom/airbnb/lottie/LottieAnimationView;->k()V
    invoke-virtual {v0}, Lcom/airbnb/lottie/LottieAnimationView;->l()V
    const/high16 v2, -0x40800000    # -1.0f
    invoke-virtual {v0, v2}, Lcom/airbnb/lottie/LottieAnimationView;->setSpeed(F)V
    const/16 v2, 0x1a
    invoke-virtual {v0, v2, v1}, Lcom/airbnb/lottie/LottieAnimationView;->p(II)V
    invoke-virtual {v0, v1}, Lcom/airbnb/lottie/LottieAnimationView;->setFrame(I)V
    invoke-virtual {v0, v3}, Lcom/airbnb/lottie/LottieAnimationView;->setRepeatCount(I)V
    new-instance v1, Lcj/h;
    const/16 v2, 0x9
    invoke-direct {v1, v2, v0, p0}, Lcj/h;-><init>(ILjava/lang/Object;Ljava/lang/Object;)V
    invoke-virtual {v0, v1}, Lcom/airbnb/lottie/LottieAnimationView;->a(Landroid/animation/Animator$AnimatorListener;)V
    invoke-virtual {v0}, Lcom/airbnb/lottie/LottieAnimationView;->j()V
    :cond_9e
    return-void
