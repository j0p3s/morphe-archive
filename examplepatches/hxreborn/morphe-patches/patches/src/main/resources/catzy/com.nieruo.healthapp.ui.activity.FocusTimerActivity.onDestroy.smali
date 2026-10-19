    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->y:Lkotlinx/coroutines/e2;
    const/4 v1, 0x0
    if-eqz v0, :cond_8
    invoke-virtual {v0, v1}, Lkotlinx/coroutines/w1;->b(Ljava/util/concurrent/CancellationException;)V
    :cond_8
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->z:Lkotlinx/coroutines/e2;
    if-eqz v0, :cond_f
    invoke-virtual {v0, v1}, Lkotlinx/coroutines/w1;->b(Ljava/util/concurrent/CancellationException;)V
    :cond_f
    const/4 v0, 0x0
    invoke-virtual {p0, v0}, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->f0(Z)V
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->k:Landroid/animation/AnimatorSet;
    invoke-virtual {v0}, Landroid/animation/AnimatorSet;->cancel()V
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->l:Landroid/animation/AnimatorSet;
    invoke-virtual {v0}, Landroid/animation/AnimatorSet;->cancel()V
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->f:Landroid/os/Handler;
    invoke-virtual {v0, v1}, Landroid/os/Handler;->removeCallbacksAndMessages(Ljava/lang/Object;)V
    iget-object v0, p0, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->v:Lkotlinx/coroutines/e2;
    if-eqz v0, :cond_29
    invoke-virtual {v0, v1}, Lkotlinx/coroutines/w1;->b(Ljava/util/concurrent/CancellationException;)V
    :cond_29
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->k0()Landroid/animation/AnimatorSet;
    move-result-object v0
    invoke-virtual {v0}, Landroid/animation/AnimatorSet;->cancel()V
    sget-object v0, Lsi/c;->a:Ljava/util/ArrayList;
    invoke-static {p0}, Lsi/c;->o(Lsi/a;)V
    sget v0, Lcom/nieruo/healthapp/utils/f;->a:I
    invoke-virtual {p0}, Lcom/nieruo/healthapp/ui/activity/FocusTimerActivity;->l0()Lcom/nieruo/healthapp/entity/TempFocusEntity;
    move-result-object v1
    const-string v2, "activity_on_destroy"
    invoke-static {v2}, Lu4/h;->R(Ljava/lang/String;)V
    invoke-static {v1}, Lu4/h;->O(Lcom/nieruo/healthapp/entity/TempFocusEntity;)V
    invoke-static {p0, v0}, Lu4/h;->L(Landroid/app/Activity;I)V
    const-string v0, "activity_onDestroy"
    invoke-static {v0}, Lu4/h;->z(Ljava/lang/String;)V
    invoke-super {p0}, Li/l;->onDestroy()V
    return-void
