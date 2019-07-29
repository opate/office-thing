import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { WorkPeriodComponent } from './work-period/work-period.component'
import { UserComponent } from './user/user.component'

const routes: Routes = [
  { path:'', component: WorkPeriodComponent },
  { path:'user', component: UserComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
