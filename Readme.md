Application generates Static and Dynamic reports and put in on Google Disk.
Static report shows all files and permissions.
Dynamic report shows all actions on files.

1.
https://console.developers.google.com/
Create a project.
Enable API:
 Admin SDK		
 Apps Activity API		
 Google Ads API		
 Google Drive API	 
 Subscribe with Google Developer AP
 
2.Generate service account. Download a json.
Rename it and put in the project --> resources as "serviceAccount.json"

3. Open CronBuild class.
There are folder-exclusions to scan and strange e-mail that we decided not to mark as strange.
There are schedulers for Static and Dynamic reports.
