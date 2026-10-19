    invoke-virtual {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->getBinding()Lx4/a;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/databinding/ActivityHalloweenLightBinding;
    iget-object v0, v0, Lcom/nieruo/healthapp/databinding/ActivityHalloweenLightBinding;->f:Lcom/airbnb/lottie/LottieAnimationView;
    iget-object v0, v0, Lcom/airbnb/lottie/LottieAnimationView;->h:Lr5/a0;
    invoke-virtual {v0}, Lr5/a0;->k()Z
    move-result v0
    iput-boolean v0, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenLightActivity;->f:Z
    if-eqz v0, :cond_1d
    invoke-virtual {p0}, Lcom/nieruo/core/common/base/BaseBindingActivity;->getBinding()Lx4/a;
    move-result-object v0
    check-cast v0, Lcom/nieruo/healthapp/databinding/ActivityHalloweenLightBinding;
    iget-object v0, v0, Lcom/nieruo/healthapp/databinding/ActivityHalloweenLightBinding;->f:Lcom/airbnb/lottie/LottieAnimationView;
    invoke-virtual {v0}, Lcom/airbnb/lottie/LottieAnimationView;->i()V
    :cond_1d
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenLightActivity;->e:Landroid/media/MediaPlayer;
    const/4 v1, 0x0
    if-eqz v0, :cond_2a
    invoke-virtual {v0}, Landroid/media/MediaPlayer;->isPlaying()Z
    move-result v0
    const/4 v2, 0x1
    if-ne v0, v2, :cond_2a
    move v1, v2
    :cond_2a
    iput-boolean v1, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenLightActivity;->g:Z
    if-eqz v1, :cond_35
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenLightActivity;->e:Landroid/media/MediaPlayer;
    if-eqz v0, :cond_35
    invoke-virtual {v0}, Landroid/media/MediaPlayer;->pause()V
    :cond_35
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/HalloweenLightActivity;->d:Landroid/animation/AnimatorSet;
    if-eqz v0, :cond_3c
    invoke-virtual {v0}, Landroid/animation/AnimatorSet;->pause()V
    :cond_3c
    invoke-super {p0}, Landroidx/fragment/app/i0;->onPause()V
    return-void
