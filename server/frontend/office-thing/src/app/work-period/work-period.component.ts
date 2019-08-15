import { Component, OnInit } from '@angular/core';
import { HttpClientService, WorkPeriod } from '../service/http-client.service';

@Component({
  selector: 'app-work-period',
  templateUrl: './work-period.component.html',
  styleUrls: ['./work-period.component.css']
})

export class WorkPeriodComponent implements OnInit {

  workperiods:WorkPeriod[];

  constructor(

    private httpClientService:HttpClientService
  
    ) { }

  ngOnInit() {
    this.httpClientService.getWorkPeriods().subscribe(response =>this.handleSuccessfulResponse(response),
     ); 
  }

  handleSuccessfulResponse(response)
  {
      this.workperiods=response;
  }

  displayedColumns: string[] = ['rfid', 'workDate', 'workStart', 'workFinish', 'workDurationSeconds'];
  
}
