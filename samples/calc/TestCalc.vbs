'**********************************************************
'* A simple four function calculator, written in VBScript *
'**********************************************************

' *** create a result window
Set result = createBean("java.awt.TextField", "0")

' *** create a panel of buttons
Set panel = createBean("java.awt.Panel")
panel.setLayout createBean("java.awt.GridLayout", 4, 4)

buttons = "789*456/123-C0.+"
For i = 1 To len(buttons)
   label = mid(buttons,i,1)
   Set button = createBean("java.awt.Button", label)
   panel.add button

   If InStr("*/-+", label) Then
     button.onAction="op """ & label & """"
   Elseif label="C" Then
     button.onAction="mem=0 : nextOp=""+"" : result.text=""0"""
   Else 
     button.onAction="press """ & label & """"
   End If
Next 

' *** Place everything in the frame
frame.title = "VBScript Calc"
frame.resize 130, 200
frame.add "North", result 
frame.add "Center", panel 
frame.validate

' *** Initialize the state of the calculator
mem = 0
nextOp = "+"
autoClear = True

' *** handle data entry keys
Sub press (key)
   On Error Resume Next
   If autoClear Then result.text="0"
   If result.text="0" and key<>"." Then result.text = ""
   If key="." and InStr(result.text,".") Then key=""
   result.text = result.text & key
   autoClear=False
End sub

' *** handle arithmetic keys
Sub op (key)
   err=0
   On Error Resume Next
   num = result.text+0
   If nextOp = "+" Then mem = mem + num
   If nextOp = "-" Then mem = mem - num
   If nextOp = "*" Then mem = mem * num
   If nextOp = "/" Then mem = mem / num
   nextOp = key

   result.text = "" & mem
   If err>0 then result.text="ERROR" : mem=0 : nextOp="+"
   autoClear=True
End sub
