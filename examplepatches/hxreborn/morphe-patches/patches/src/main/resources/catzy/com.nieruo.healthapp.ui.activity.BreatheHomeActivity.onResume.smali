    invoke-super {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->onResume()V
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/BreatheHomeActivity;->r0()V
    invoke-virtual {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->getBinding()Lx4/a;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/databinding/ActivityBreatheHomeBinding;
    iget-object v0, v0, Lcom/nieruo/healthapp/databinding/ActivityBreatheHomeBinding;->i:Landroidx/constraintlayout/widget/ConstraintLayout;
    invoke-virtual {v0}, Landroid/view/View;->isClickable()Z
    move-result v0
    if-nez v0, :cond_62
    sget-object v0, Lcom/nieruo/healthapp/manager/a1;->m:Lfe/d;
    invoke-virtual {v0}, Lfe/d;->t()Lcom/nieruo/healthapp/manager/a1;
    move-result-object v0
    invoke-virtual {v0}, Lcom/nieruo/healthapp/manager/a1;->D()Z
    move-result v0
    if-eqz v0, :cond_2e
    invoke-virtual {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->getBinding()Lx4/a;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/databinding/ActivityBreatheHomeBinding;
    iget-object v0, v0, Lcom/nieruo/healthapp/databinding/ActivityBreatheHomeBinding;->i:Landroidx/constraintlayout/widget/ConstraintLayout;
    const/16 v1, 0x8
    invoke-virtual {v0, v1}, Landroid/view/View;->setVisibility(I)V
    return-void
    :cond_2e
    invoke-virtual {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->getBinding()Lx4/a;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/databinding/ActivityBreatheHomeBinding;
    iget-object v0, v0, Lcom/nieruo/healthapp/databinding/ActivityBreatheHomeBinding;->z:Lcom/airbnb/lottie/LottieAnimationView;
    invoke-virtual {v0}, Lcom/airbnb/lottie/LottieAnimationView;->getComposition()Lr5/l;
    move-result-object v1
    if-eqz v1, :cond_62
    iget v1, v1, Lr5/l;->m:F
    float-to-int v1, v1
    invoke-virtual {v0}, Lcom/airbnb/lottie/LottieAnimationView;->k()V
    invoke-virtual {v0}, Lcom/airbnb/lottie/LottieAnimationView;->l()V
    const/high16 v2, -0x40800000    # -1.0f
    invoke-virtual {v0, v2}, Lcom/airbnb/lottie/LottieAnimationView;->setSpeed(F)V
    const/16 v2, 0x1a
    invoke-virtual {v0, v2, v1}, Lcom/airbnb/lottie/LottieAnimationView;->p(II)V
    invoke-virtual {v0, v1}, Lcom/airbnb/lottie/LottieAnimationView;->setFrame(I)V
    const/4 v1, 0x0
    invoke-virtual {v0, v1}, Lcom/airbnb/lottie/LottieAnimationView;->setRepeatCount(I)V
    new-instance v1, Lcj/h;
    const/4 v2, 0x7
    invoke-direct {v1, v2, v0, p0}, Lcj/h;-><init>(ILjava/lang/Object;Ljava/lang/Object;)V
    invoke-virtual {v0, v1}, Lcom/airbnb/lottie/LottieAnimationView;->a(Landroid/animation/Animator$AnimatorListener;)V
    invoke-virtual {v0}, Lcom/airbnb/lottie/LottieAnimationView;->j()V
    :cond_62
    return-void
