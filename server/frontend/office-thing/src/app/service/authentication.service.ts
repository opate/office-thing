import { Injectable } from '@angular/core';
import { HttpClient,HttpHeaders } from '@angular/common/http';
import { map } from 'rxjs/operators';

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

  constructor(
    private httpClient:HttpClient
  ) { 
     }

  authenticate(username, password) {
    const headers = new HttpHeaders({ Authorization: 'Basic ' + btoa(username + ':' + password) });
    /*
     http://127.0.0.1:9090/ui/workinghours/validateLogin'
    */
    return this.httpClient.get<User>('https://www.pateweb.de/ui/workinghours/validateLogin',{headers}).pipe(
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
    console.log(!(user === null))
    return !(user === null)
  }

  logOut() {
    sessionStorage.removeItem('username')
  }

}