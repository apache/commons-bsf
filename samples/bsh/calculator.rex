/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
/* A silly little calculator implemented in Object Rexx using
   Java components for the UI. ("bsf\samples\bsh\calculator.js" served as an example)

   ooRexx (FOSS):   <http://www.ooRexx.org>
   BSF4Rexx (FOSS): <http://wi.wu-wien.ac.at/rgf/rexx/bsf4rexx/current/> or
                    eventually at <https://sourceforge.net/projects/bsf4rexx>

*/

if      BsfInvokedBy()=1 then say "This Rexx program was invoked by Java!"
else if BsfInvokedBy()=2 then say "This Rexx program was invoked by Rexx, JVM loaded by Rexx!"
else                          say "No JVM present, we got troubles ..."

.bsf~bsf.import("java.awt.TextField", "awtTextField")
.bsf~bsf.import("java.awt.Label"    , "awtLabel"    )

f = .bsf~new("java.awt.Frame", "BSH Calculator (ooRexx)")
f~bsf.addEventListener("window", "windowClosing", "call bsf 'exit'")

f1 = .awtTextField~newStrict("int", 20)   -- "newStrict" to force the constructor with the "int" argument
f1~bsf.addEventListener("action", "", "call doMath /* action event */")

f2 = .awtTextField~newStrict("int", 20)
f2~bsf.addEventListener("text", "", "call doMath /* text event */")

p = .bsf~new( "java.awt.Panel") ~~setLayout(.bsf~new("java.awt.GridLayout", 2, 2))
p ~~add(.awtLabel~new("Enter operand")) ~~add(f1)
p ~~add(.awtLabel~new("Enter operand")) ~~add(f2)

f ~~add("North", p) ~~add("Center", .awtLabel~new("Results:"))

p =  .bsf~new("java.awt.Panel") ~~setLayout(.bsf~new("java.awt.GridLayout", 4, 2))

sum= .awtTextField~newStrict("int", 20)
p ~~add(.awtLabel~new("Sum")) ~~add(sum)

diff= .awtTextField~newStrict("int", 20)
p ~~add(.awtLabel~new("Difference")) ~~add(diff)

prod= .awtTextField~newStrict("int", 20)
p ~~add(.awtLabel~new("Product")) ~~add(prod)

quo = .awtTextField~newStrict("int", 20)
p ~~add(.awtLabel~new("Quotient")) ~~add(quo)

f ~~add("South", p) ~~pack ~~show ~~toFront

do forever
   interpret .bsf~bsf.pollEventText -- retrieve eventText and interpret it
end
exit

getField: procedure
   use arg f
   t=f~getText
   if t="" then return 0
   return t

doMath:
   n1 = getField(f1); if n1="-" then n1=-1;else if n1="+" then n1=1
   n2 = getField(f2); if n2="-" then n2=-1;else if n2="+" then n2=1
   sum ~setText(n1+n2)
   diff~setText(n1-n2)
   prod~setText(n1*n2)
   if n2=0 then quo~setText("DIVISION by 0 !!!")
           else quo~setText(n1/n2)
   return

::requires BSF.CLS    -- get Object Rexx wrapper support
