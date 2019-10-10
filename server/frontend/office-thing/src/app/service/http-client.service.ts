import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { environment } from '../../environments/environment';

/*
@Injectable({
  providedIn: 'root'
})
*/

export class WorkPeriod{
  constructor(
    public rfidUid: number,
    public workDate: string,
    public workStart: string,
    public workFinish: string,
    public workDuration: string
  ) {}
}

export class HttpClientService {

  baseUrl = environment.baseUrl;

  constructor(

    private httpClient:HttpClient
  
    ) { }

  getWorkPeriods()
  {
    let url = '/workperiod';
    return this.httpClient.get<WorkPeriod[]>(url);
  }
}
