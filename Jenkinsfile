node ('master'){
  stage 'Build and Test'
  env.PATH = "${tool 'SBT 0.13.12'}/bin:${env.PATH}"
  checkout scm
  sh 'sbt -mem 512 clean package'
  sh 'sbt -mem 512 testNoHttpServer'
  sh 'sbt -mem 512 cebesHttpServer/test'
 }