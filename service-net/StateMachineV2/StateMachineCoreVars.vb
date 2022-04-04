Imports System.IO

Public Class StateMachineCoreVars
    Public primTripTable As New List(Of PrimaryMSG)
    Public resposibleDictionary As Dictionary(Of String, String)

    Public Sub New()
        LoadPrimaryTripTable()
        ResposibleTable()
    End Sub


    Public Function LoadPrimaryTripTable()
        If (primTripTable IsNot Nothing And primTripTable.Count <> 0) Then
            Return primTripTable
        End If

        Dim dt As New List(Of PrimaryMSG)

        'load primary trip
        'Dim csvPath As String = Path.GetFileName("PrimaryTripTable.csv")
        ''Read the contents of CSV file.  
        'Dim csvData() As String = File.ReadAllLines(csvPath)


        Dim csvData() As String = GetEmbeddedResourceLines("GE.Myplant.StateMachineV2.PrimaryTripTable.csv")

        Console.WriteLine("CSV DATA Prim " & csvData.Length)

        'Execute a loop over the rows.  
        For Each row As String In csvData

            If Not String.IsNullOrEmpty(row) Then
                Dim parts As String() = row.Split(";"c)
                dt.Add(New PrimaryMSG With {
                                    .MSGCombination = parts(0),
                                    .MSGNo = parts(1)
                                     })
            End If
        Next

        primTripTable = dt
        Return primTripTable
    End Function


    Public Function ResposibleTable()
        If (resposibleDictionary IsNot Nothing) Then
            Return resposibleDictionary
        End If


        Dim dictionary As New Dictionary(Of String, String)
        resposibleDictionary = dictionary

        ''load primary trip
        'Dim csvPath As String = Path.GetFileName("Responsibility.csv")
        ''Read the contents of CSV file.  
        'Dim csvData() As String = File.ReadAllLines(csvPath)

        Dim csvData() As String = GetEmbeddedResourceLines("GE.Myplant.StateMachineV2.Responsibility.csv")

        Console.WriteLine("CSV DATA Resp " & csvData.Length)


        'Execute a loop over the rows.  
        For Each row As String In csvData
            If Not String.IsNullOrEmpty(row) Then
                Dim split() = row.Split(";"c)
                dictionary.Add(split(0), split(2))
            End If
        Next

        resposibleDictionary = dictionary
        Return resposibleDictionary
    End Function




    ''' 

    ''' Returns a string containing the contents of the specified embedded resource from the executing assembly.
    ''' 
    ''' The name of the file of the embedded resource.  This should include the root namespace preceding the file name.
    ''' A string with the contents of the embedded resource.
    ''' 
    Public Function GetEmbeddedResourceLines(name As String) As String()
        Return GetEmbeddedResource(System.Reflection.Assembly.GetExecutingAssembly, name).Split(New String() {Environment.NewLine},
                                       StringSplitOptions.None)
    End Function

    ''' 

    ''' Returns a string containing the contents of the specified embedded resource from the provided assembly.
    ''' 
    ''' The System.Reflection.Assembly object you want to get the embedded resource from.
    ''' The name of the file of the embedded resource.  This should include the root namespace preceding the file name.
    ''' A string with the contents of the embedded resource.
    ''' 
    Public Function GetEmbeddedResource(assembly As System.Reflection.Assembly, name As String) As String
        Dim buf As String = ""
        Using s As System.IO.Stream = assembly.GetManifestResourceStream(name)
            Using sr As New System.IO.StreamReader(s)
                buf = sr.ReadToEnd
                sr.Close()
            End Using
            s.Close()
        End Using
        Return buf
    End Function

End Class
