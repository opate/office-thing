import { Injectable } from '@angular/core';
import { HttpClient,HttpHeaders } from '@angular/common/http';
import { map } from 'rxjs/operators';
import { environment } from '../../environments/environment';

export class User{
  constructor(
    public customerId: number,
    public email: string,
    public name: string,
    public given_name: string,
    public role:string,
     ) {}
  
}

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  baseUrl = environment.baseUrl;

  constructor(
    private httpClient:HttpClient
  ) { 
     }

  authenticate(username, password) {
    const headers = new HttpHeaders({ Authorization: 'Basic ' + btoa(username + ':' + password) });
    
    let url = '/validateLogin';
    return this.httpClient.get<User>(url,{headers}).pipe(
        map(
          userData => {
            sessionStorage.setItem('username',username);
            let authString = 'Basic ' + btoa(username + ':' + password);
            sessionStorage.setItem('basicauth', authString);    
            return userData;
          }
        )
    );
  }

  isUserLoggedIn() {
    let user = sessionStorage.getItem('username')
    return !(user === null)
  }

  logOut() {
    sessionStorage.removeItem('username')
  }

}