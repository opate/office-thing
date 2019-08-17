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
    public workDuration: string
  ) {}
}

export class HttpClientService {

  constructor(

    private httpClient:HttpClient
  
    ) { }

  getWorkPeriods()
  {
    return this.httpClient.get<WorkPeriod[]>('https://www.pateweb.de/ui/workinghours/workperiod');
    /*
    return this.httpClient.get<WorkPeriod[]>('https://127.0.0.1:9090/ui/workinghours/workperiod');
    */
  }
}
