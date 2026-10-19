    invoke-super {p0}, Li/l;->onDestroy()V
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/WeeklyActivity;->d:Landroid/animation/AnimatorSet;
    if-eqz v0, :cond_a
    invoke-virtual {v0}, Landroid/animation/AnimatorSet;->cancel()V
    :cond_a
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/WeeklyActivity;->e:Landroid/animation/AnimatorSet;
    if-eqz v0, :cond_11
    invoke-virtual {v0}, Landroid/animation/AnimatorSet;->cancel()V
    :cond_11
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/WeeklyActivity;->f:Landroid/animation/AnimatorSet;
    invoke-virtual {v0}, Landroid/animation/AnimatorSet;->cancel()V
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/WeeklyActivity;->g:Landroid/animation/ObjectAnimator;
    if-eqz v0, :cond_1d
    invoke-virtual {v0}, Landroid/animation/Animator;->cancel()V
    :cond_1d
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/WeeklyActivity;->h:Landroid/animation/AnimatorSet;
    if-eqz v0, :cond_24
    invoke-virtual {v0}, Landroid/animation/AnimatorSet;->cancel()V
    :cond_24
    return-void
