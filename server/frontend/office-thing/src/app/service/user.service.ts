import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { User } from "../model/user.model";

@Injectable({
  providedIn: 'root'
})

export class UserService {

  constructor(

    private httpClient:HttpClient
  
    ) { }

    getUserDetails()
    {
      let url = '/validateLogin';
      return this.httpClient.get<User>(url);
    }

}
