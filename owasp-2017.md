# Vulnerability Analysis
Samenvatting van de OWASP top 10 met af en toe een voorbeeld uit het project.

## A1:2017 Injection

### Description
Injection is het injecteren van bijvoorbeeld een SQL-statement door het mee te sturen in een input veld of request.
Bijna elke soort input in web forms of URLS zijn hier gevoelig voor.
Nieuwe frameworks beschermen je vaak tegen deze vorm van injectie.

### Risk
De impact is groot. Statements als "DROP TABLE" kunnen ervoor zorgen dat je data kwijt raakt.
"UPDATE" statements kunnen je data aanpassen zonder dat jij dat wil. 
Tevens kun je (gevoelige) data uit het systeem inzien; een data lek.

Authenticatie kan ervoor zorgen dat je in de logging kan zien wie deze statements uitvoerd.
Dit betekent wel dat je dit soort requests moet loggen en dat de logs los staan
van de databases waar de injecties op uitgevoerd worden. Anders kan de aanvaller deze data alsnog verwijderen.

Authorisatie zou beter kunnen helpen. DROP/UPDATE statements kunnen dan bijvoorbeeld alleen
door Admin gebruikers worden gedaan en niet door elke user input. 

### Counter-measures
Fuzzing-tests kunnen je helpen om je systeem veiliger te maken. Deze vorm van testen is
bedoeld om programmeerfouten of beveiligingsgaten te vinden. Er zijn verschillende frameworks/libraries die dit voor je kunnen doen zoals: 

Je kan bijvoorbeeld MvC tests opstellen met bepaalde SQL-statements als Strings en zo proberen
gaten te vinden in je systeem.

Validatie van ingevoerde gegevens van de gebruiker kan deze vulnerability verhelpen. Zorg ervoor
dat de gebruikers-invoer niet rechtstreeks als query wordt meegegeven. Denk aan Prepared statements.


## A2:2017 Broken Authentication

### Description
Broken authentication heeft te maken met het authenticatie beleid binnen je systeem. Dit is het proces
is het proces waarbij iemand nagaat of een gebruiker, een andere computer of applicatie daadwerkelijk is wie hij beweert te zijn.



### Risk
Hier een aantal scenario's:

Het gebruik van moeilijke tekens in je wachtwoord wordt vaak gezien als betere beveiliging.
Je kunt bijvoorbeeld verplicht stellen dat een wachtwoord een hoofdletter, kleine letter, leesteken en cijfer moet bevatten.
Het punt is dat korte wachtwoorden vaak te raden zijn met een dictionary attack.
De leestekens hebben weinig effect als je wachtwoord uit 5 characters bestaat.

Een ander voorbeeld is het gebruiken van dezelfde usernames en passwords bij verschillende services.
Als er dan één datalek plaatsvind bij een service ben je meteen ook aan te vallen op andere services.

Nog een voorbeeld is het gebruik van alleen een wachtwoord. Tegenwoordig kun je ook meerdere authenticatie methoden
gebruiken zoals een SMS bij het inloggen of een token generator. Dit zorgt ervoor dat je met alleen het wachtwoord niet zoveel kan.

Het laatste voorbeeld is het gebruik van sessies. Als 1x inlogd en vervolgens altijd de gebruiker onthoudt kan dit voor problemen zorgen.
Je hebt immers maar 1x geauthorizeerd of dat wel echt die persoon is. Tevens kan het zijn dat de persoon is ingelogd op een openbare
computer en de tweede gebruiker erna nog ingelogd staat als de eerste persoon.

### Counter-measures
Tegenmaatregelen kunnen bijvoorbeeld zijn:

Zorg er in ieder geval voor dat een dictionary attack minder makkelijk wordt door een limiet in te stellen op het raden van wachtwoorden.
Bijvoorbeeld dat elk uur maar 5x een fout wachtwoord ingevoerd mag worden.
Een wachtwoordbeleid wat ingericht is op langere wachtwoorden i.p.v. korte wachtwoorden met leestekens.
MFA (Multi factor authentication) implementeren door naast een wachtwoord ook een sms bevestiging te moeten doen.
Session management zo instellen dat de gebruiker elke x aantal uren opnieuw moet inloggen.


## A3:2017 Sensitive Data Exposure
 
### Description
Sensitive data exposure gaat over het behandelen van gevoelige data in je systeem. Denk hierbij aan
wachtwoorden, maar ook creditcard gegevens en persoonlijke informatie. 

Een belangrijke factor is het encrypten van gegevens, zodat aanvallers die bijvoorbeeld je verkeer onderscheppen
niet zomaar kunnen inzien welke data je verstuurd. Daarbij kun je gegevens ook encrypted opslaan. Zo kun je niet rechtstreeks
wachtwoorden inzien van de database, maar dien je deze eerst te 'ontsleutelen' met een key.

### Risk
Het risico van dataverkeer versturen over bijvoorbeeld HTTP is dat het heel makkelijk is voor andere om deze pakketjes te onderscheppen
en in te zien. Ook inloggegevens kunnen onderschept worden, wat je authenticatie schaad. Een risico van wachtwoorden in plain-tekst opslaan is dat je met een query gemakkelijk alle wachtwoorden kan inzien.
Hetzelfde geldt voor persoonsgegevens en/of creditcard gegevens.


### Counter-measures
De nieuwe GDPR stelt verplicht dat je bepaalde security measures moet toepassen als je gevoelige data in je systeem opslaat.
Daarbij kun je ook stellen dat je gevoelige data zo min mogelijk wil opslaan en ook zo kort mogelijk. Als het er niet is kan het ook niet lekken.

Encryptie is de belangrijkste schakel. Zorg dat zowel je gegevensverkeer als opgeslagen gegevens op een sterke manier ge-encrypt zijn.
Richt daarbij ook je key-management veilig in.

Github biedt ook een aantal tools om je te beschermen tegen bijvoorbeeld het committen van API KEYS of andere gevoelige informatie. 

## A4:2017 XML External Entities

???

## A5:2017 Broken Acces Control

### Description
Dit gaat over het verifiëren van bepaalde acties. Als ik als gebruiker mijn profiel wil zien
dan kun je dat requesten op basis van mijn userId. Maar als ik in de URL gewoon mijn userId kan aanpassen
en zo andere profielen kan zien is dat een vulnerability.

### Risk
Je kan dus je systeem wel inrichten op het authenticeren van gebruikers door in te loggen, maar het is belangrijk
dat je dus ook bij requests verifieerd of dit wel die gebruiker is. Als ik me tevens kan voordoen als een 
administrator is je authorizatie ook omzeild.

### Counter-measures
Maak geen gebruik van op-een-volgende userId's. Zo kan ik in mijn url niet gokken op Id's. Verder dien je bij requests
ook te verifiëren van wie deze komt. Zo kun je voorkomen dat een normale user admin requests kan doen.

## A6:2017 Security Misconfiguration

### Description
Hierbij kun je denken aan het intact laten van standaardgegevens zoals een default admin account, default wachtwoorden of url's.
De meeste Postgres databases zijn standaard voorzien van een account 'Postgres' met wachtwoord 'Postgres'. Standaard staat je systeem
dus vaak niet ingesteld op de meest beveiligde configuratie. 

Het gebeurt ook vaak bij het implementeren van Security features om deze vervolgens te omzeilen door een omweg in te bouwen.
Zo hebben je security features geen zin meer.

Ook oude packages of frameworks kunnen niet goed genoeg beveiligd zijn op security vulnerabilities die vandaag de dag belangrijk de kop op doen. 

### Risk
Met een standaard Admin account (met als wachtwoord 'admin') omzeil je meteen al je authenticatie en authorizatie. Ook door het inbouwen
van een zogenaamde 'back-door' breng je je hele systeem in gevaar. Het is dus belangrijk dat je deze standaard-instellingen 
aanpast als je applicatie naar productie gaat. Je kunt hiervoor checks plannen in je CI pipeline. Hetzelfde geldt voor oude versies van software.

### Counter-measures
Richt je CI/CD pipeline zo in dat er checks worden uitgevoerd op deze vulnerabilities. Wij gebruiken bijvoorbeeld DependaBot die het implementeren
van nieuwe versies van dependencies gemakkelijk maakt en toevoegd in je pipeline.

Check ook regelmatig of je dependencies nog wel gebruikt of niet. Overbodige dependencies leveren niets op.


## A7:2017 Cross-Site Scripting

### Description
Dit lijkt een beetje op injecteren. In het geval van cross-site scripting heb je ook te maken met user input die 
op een gevaarlijke manier gebruikt kan worden. Users kunnen bijvoorbeeld een script invoeren die de session data van de gebruiker
ophaald en verstuurd. Als deze dubieuze code dan geplaatst wordt op een publiek forum wordt bij elke gebruiker die deze pagina
laadt zijn session data verstuurd naar de attacker.

### Risk
Dit kan ervoor zorgen dat andere gebruikers van bijvoorbeeld dit forum hun gegevens automatisch versturen naar de attacker zonder
dat zij dit door hebben. Met session-data kan de attacker zich tevens voordoen als anderen.


### Counter-measures
Het encoden van user input om dit soort malifide code onschadelijk te maken.

## A8:2017 Insecure Deserialization

### Description
Dit heeft te maken met het rondsturen van data. Bij ons project zou je bijvoorbeeld het hele game object kunnen
serialiseren en rondsturen. Dit zorgt er wel voor dat je misschien ook data van de game mee stuurt die de gebruiker niet mag inzien.
Wat dacht je van het woord dat hij/zij moet raden. Dan wordt het spel wel makkelijk.

De oplossing is het versturen van JSON waar in tekst alleen de gegevens staan die je wilt versturen. Hiervoor gebruik ik het DTO.

### Risk
In ons lingo spel zou je het woord dus kunnen zien. Veel gevaarlijker wordt het als je rollen of user data rechstreeks
serialiseerd en deserialiseerd. Een attacker verandert bijvoorbeeld het geserialiseerde object
om een rol te veranderen van gebruiker naar administrator.


### Counter-measures
Objecten niet rechtstreeks serialiseren en rondsturen maar converteren naar JSON dat alleen de data bevat die je wilt versturen.


## A9:2017 Using Components with Known Vulnerabilities

### Description
Ons systeem is opgebouwd uit verschillende componenten. We gebruiken Spring-boot, dat gebruikt weer Hibernate en zo zijn er meer componenten.
Deze componenten dienen up-to-date zijn. Ons systeem kan goed beveiligd zijn maar als er een backdoor zit
in Hibernate dan maakt dat niet uit.

### Risk
Components met backdoors kunnen de security measures van je systeem onderuithalen.

### Counter-measures
Verwijder dependencies die je niet gebruikt. Dit kan alleen maar lekken opleveren.

Wij gebruiken DependaBot die onze dependencies checkt op verouderde versies en vulnerabilities. We gebruiken
ook SonarCloud die onze repository scant op vulnerabilities.

Wees zuinig met het downloaden van niet geverifieerde plugins.



## A10:2017 Insufficient Logging & Monitoring

### Description
Het is belangrijk dat je vulnerabilities in je systeem vermijdt. Maar het is ook belangrijk op de hoogte
gehouden te worden als deze er wel zijn. Dit kun je afvangen met loggen en monitoren.

Door thresholds in te stellen kun je op de hoogte gehouden worden bepaalde dubieuze acties. Als er bijvoorbeeld
1000 lingo games worden aangemaakt in 2 seconden dan kun je ervan uitgaan dat er iets mis is (of mijn spel is 
in 1x heel populair).

Het hoeft niet alleen te gaan om enorme aantallen. Het kan ook zijn dat iemand elke keer inlogd en alleen een game start
en verder niet speelt. Je kunt dit soort apart gedrag ook loggen om later te analyseren waarom dit gebeurt.

### Risk
Wanneer je niet op de hoogte wordt gehouden van dubieuze acties kunnen vulnerabilities in je systeem misschien
wel jaren misbruikt worden zonder dat jij het door hebt.
Assessment of risk. Discussion of authentication and authorization.

### Counter-measures
Het loggen en monitoren van bepaald gedrag met genoeg context, zodat je deze gegevens later kan analyseren
en risico's van je systeem in kaart kunt brengen. Stel ook Alerts in bij kritieke thresholds zodat je op de hoogte wordt gehouden
wanneer je systeem faalt.