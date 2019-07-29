import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { WorkPeriodComponent } from './work-period.component';

describe('WorkPeriodComponent', () => {
  let component: WorkPeriodComponent;
  let fixture: ComponentFixture<WorkPeriodComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ WorkPeriodComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(WorkPeriodComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
