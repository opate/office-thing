
import { Component, Inject, Optional } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material';
import { HttpClientService } from '../service/http-client.service';
 
export interface UsersData {
  name: string;
  id: number;
}
 
@Component({
  selector: 'app-dialog-box',
  templateUrl: './dialog-box.component.html',
  styleUrls: ['./dialog-box.component.css']
})
export class DialogBoxComponent {
 
  action:string;
  local_data:any;
 
  constructor(
    public dialogRef: MatDialogRef<DialogBoxComponent>,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: UsersData,
    private httpClientService:HttpClientService,) {
    console.log(data);
    this.local_data = {...data};
    this.action = this.local_data.action;
  }
 
  handleSuccessfulResponse(response)
  {
      console.log("http response: " + response);
  }

  doAction(){
    if (this.action == 'Delete')
    {
      console.log("workperiod id is:" + this.data.id);
      this.httpClientService.deleteWorkPeriod(this.data.id).subscribe(response =>this.handleSuccessfulResponse(response),
      ); 
    }
    if (this.action == 'Edit')
    {
      console.log("Functionality EDIT is not yet implemented bro")
    }
    this.dialogRef.close({event:this.action,data:this.local_data});
  }
 
  closeDialog(){
    this.dialogRef.close({event:'Cancel'});
  }
 
}