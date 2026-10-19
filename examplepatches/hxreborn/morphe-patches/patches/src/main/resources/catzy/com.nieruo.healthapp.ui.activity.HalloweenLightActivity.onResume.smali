    invoke-super {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->onResume()V
    iget-boolean v0, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenLightActivity;->f:Z
    if-eqz v0, :cond_12
    invoke-virtual {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->getBinding()Lx4/a;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/databinding/ActivityHalloweenLightBinding;
    iget-object v0, v0, Lcom/nieruo/healthapp/databinding/ActivityHalloweenLightBinding;->f:Lcom/airbnb/lottie/LottieAnimationView;
    invoke-virtual {v0}, Lcom/airbnb/lottie/LottieAnimationView;->o()V
    :cond_12
    iget-boolean v0, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenLightActivity;->g:Z
    if-eqz v0, :cond_1d
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenLightActivity;->e:Landroid/media/MediaPlayer;
    if-eqz v0, :cond_1d
    invoke-virtual {v0}, Landroid/media/MediaPlayer;->start()V
    :cond_1d
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenLightActivity;->d:Landroid/animation/AnimatorSet;
    if-eqz v0, :cond_24
    invoke-virtual {v0}, Landroid/animation/AnimatorSet;->resume()V
    :cond_24
    const/4 v0, 0x0
    iput-boolean v0, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenLightActivity;->f:Z
    iput-boolean v0, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenLightActivity;->g:Z
    return-void
