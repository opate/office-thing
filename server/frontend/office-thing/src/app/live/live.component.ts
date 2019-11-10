import { Component, OnInit } from '@angular/core';
import { WebSocketAPI } from './WebSocketAPI';

@Component({
  selector: 'app-live',
  templateUrl: './live.component.html',
  styleUrls: ['./live.component.css']
})

export class LiveComponent implements OnInit {

  webSocketAPI: WebSocketAPI;
  greeting: any;
  name: string;
  
  ngOnInit() {
    this.webSocketAPI = new WebSocketAPI(new LiveComponent());
  }

  connect(){
    this.webSocketAPI._connect();
  }

  disconnect(){
    this.webSocketAPI._disconnect();
  }

  sendMessage(){
    this.webSocketAPI._send(this.name);
  }

  handleMessage(message){
    this.greeting = message;
  }

}
