import { Component, OnInit } from '@angular/core';
import { User } from '../model/user.model';
import { UserService } from '../service/user.service'

@Component({
  selector: 'app-user',
  templateUrl: './user.component.html',
  styleUrls: ['./user.component.css']
})

export class UserComponent implements OnInit {

  currentUser : User = new User();

  constructor(
    private userService : UserService
  ) { }

  ngOnInit() {
    this.userService.getUserDetails().subscribe(response =>this.handleSuccessfulResponse(response),
     ); 
  }

  handleSuccessfulResponse(response)
  {
      this.currentUser=response;
  }

}
