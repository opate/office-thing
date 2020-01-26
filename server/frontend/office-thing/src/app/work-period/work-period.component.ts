import { Component, OnInit } from '@angular/core';
import { HttpClientService, WorkPeriod } from '../service/http-client.service';
import { DialogBoxComponent } from '../dialog-box/dialog-box.component';
import { MatDialog } from '@angular/material';

@Component({
  selector: 'app-work-period',
  templateUrl: './work-period.component.html',
  styleUrls: ['./work-period.component.css']
})

export class WorkPeriodComponent implements OnInit {

  workperiods:WorkPeriod[];

  constructor(

    private httpClientService:HttpClientService,
    public dialog: MatDialog
  
    ) {
      dialog.afterAllClosed
      .subscribe(() => {
      // update a variable or call a function when the dialog closes
        this.getData();
      }
    );

     }

  ngOnInit() {
      this.getData(); 
  }

  handleSuccessfulResponse(response)
  {
      this.workperiods=response;
  }

  displayedColumns: string[] = ['rfid', 'workDate', 'workStart', 'workFinish', 'workDuration', 'action'];
  

  openDialog(action,obj) {
    obj.action = action;
    const dialogRef = this.dialog.open(DialogBoxComponent, {
      width: '250px',
      data:obj
    });
 
  } 

  getData()
  {
    this.httpClientService.getWorkPeriods().subscribe(response =>this.handleSuccessfulResponse(response),
    );
  }
}
