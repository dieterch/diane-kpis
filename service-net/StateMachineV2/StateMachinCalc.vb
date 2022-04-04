Imports System.Threading
Imports GE.Myplant.Integration.NetCore.Alarm
Imports GE.Myplant.Integration.NetCore.Performance

Public Class StateMachinCalc
    Private WithEvents stateMachine As New ClassMainState
    'Private PrimaryMSGList As New List(Of PrimaryMSG)
    'Private WithEvents DBF As New ClassDBFunctions
    Private MSG_List As New List(Of MSG)
    'Private Engine_List As New List(Of EngineList)
    Private Rowcount As Integer = 0
    'Dim AMMDS As New myDS.TabAMM_MyPlantDataTable

    Public Shared stateMachineCoreVars As StateMachineCoreVars

    Public Sub New()
        If (stateMachineCoreVars Is Nothing) Then
            stateMachineCoreVars = New StateMachineCoreVars
        End If
    End Sub


    Public Function doCalc(messageEvents As List(Of MessageEvent)) As List(Of DeviceStatusV2)

        stateMachine.PrimaryMSGList = stateMachineCoreVars.primTripTable

        stateMachine.Silence = True 'damit keine Meldungen, falls eine unbekannte Primary Trip Kombination gefunden wird, aus gegen wird
        stateMachine.GapMaxLength = 7300 'Maximale Zeitdifferenz in Sekunden die zwischen zwei Meldungen sein darf, damit nicht auf GAP umgeschaltet wird.


        For Each row In messageEvents
            MSG_List.Add(ConvertMessage(row))
        Next

        'stateMachine.EngineMSG_List = MSG_List 'Sobald die Meldungen geschrieben sind, wird die Berechnung gestartet.

        Dim finishedEvent As EventWaitHandle
        finishedEvent = New EventWaitHandle(False, EventResetMode.ManualReset)

        AddHandler stateMachine.Calc_Finisched, Sub(s) finishedEvent.Set()
        AddHandler stateMachine.ErrorInClass, Sub(s) finishedEvent.Set()
        'AddHandler stateMachine.CalculationError, Sub() finishedEvent.Set()
        stateMachine.EngineMSG_List = MSG_List ' calculation should start


        Thread.Sleep(2000)


        finishedEvent.WaitOne()

        Dim deviceStates As New List(Of DeviceStatusV2)
        For Each row In stateMachine.Status_List
            deviceStates.Add(ConvertDeviceStatus(row))
        Next
        Return deviceStates

    End Function

    Private Function ConvertMessage(messageIn As MessageEvent) As MSG
        Dim MSGType = "B"
        Select Case messageIn.Severity
            Case Is >= 800
                MSGType = "A"
            Case 700 To 800
                MSGType = "W"
            Case Else
                MSGType = "B"
        End Select

        Dim msgNo As Int32
        msgNo = 0
        Int32.TryParse(messageIn.Name, msgNo)
        Dim msg As New MSG With {
                        .MsgNo = msgNo,
                        .MsgDate = FromUnixTimeMilli(messageIn.Timestamp),
                        .MsgText = "",
                       .MsgType = MSGType
                       }
        Return msg
    End Function

    Private Function ConvertDeviceStatus(row As ActionDB) As DeviceStatusV2
        Dim state = New DeviceStatusV2 With
            {
            .ActionActual = row.Action_Actual,
            .ActionFrom = row.Action_From,
            .ActionTo = row.Action_To,
            .TriggerDate = row.Trigger_Date,
            .TriggerMSGNo = row.Trigger_MSGNo,
            .TriggerText = row.Trigger_Text,
            .TriggerResponsibility = row.Trigger_Responsibility,
            .TriggerCount = row.Trigger_Count,
            .DemandSelectorSwitch = row.DemandSelectorSwitch,
            .ServiceSelectorSwitch = row.ServiceSelectorSwitch,
            .AV_MAN_Activated_Status = row.AV_MAN_Activated_Status,
            .CalcDate = row.CalcDate
            }

        'If row.Trigger_Date = Nothing Then
        '    state.TriggerDate = DateTime.MinValue
        'End If

        Return state
    End Function



    Public Function FromUnixTimeMilli(unixTime As Long) As DateTime
        Dim epoch As New DateTime(1970, 1, 1, 0, 0, 0, DateTimeKind.Utc)
        Return epoch.AddMilliseconds(unixTime)
    End Function


End Class
