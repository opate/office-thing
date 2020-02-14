import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Identifiers } from '@angular/compiler';

export class UiWorkPeriod {
  id: number;
  workDuration: string;
  startWorkEvent: UiWorkEvent;
  finishWorkEvent: UiWorkEvent
}
  
export class UiWorkEvent {
  id: number;
  eventTime: string;
  clientInfo: string;
  rfidTag: string
}

export class HttpClientService {

  constructor(

    private httpClient:HttpClient
  
    ) { }

  getWorkPeriods()
  {
    let url = '/workperiod';
    return this.httpClient.get<UiWorkPeriod[]>(url);
  }

  deleteWorkPeriod(workPeriodId)
  {
    let url = "/workperiod/"+ workPeriodId;
    return this.httpClient.delete(url, {responseType: 'text'});
  }
}
