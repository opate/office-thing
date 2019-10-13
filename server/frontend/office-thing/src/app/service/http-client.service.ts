import { HttpClient, HttpHeaders } from '@angular/common/http';

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
    let url = '/workperiod';
    return this.httpClient.get<WorkPeriod[]>(url);
  }
}
