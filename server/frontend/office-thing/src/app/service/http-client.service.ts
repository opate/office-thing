import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Injectable({
  providedIn: 'root'
})

export class WorkPeriod{
  constructor(
    public rfidUid: number,
    public workDate: string,
    public workStart: string,
    public workFinish: string,
    public workDurationSeconds: number
  ) {}
}

export class HttpClientService {

  constructor(

    private httpClient:HttpClient
  
    ) { }

  getWorkPeriods()
  {
    return this.httpClient.get<WorkPeriod[]>('http://localhost:9090/allworkperiods');
  }
}
