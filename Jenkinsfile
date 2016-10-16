node ('master'){
  stage 'Build and Test'
  env.PATH = "${tool 'SBT 0.13.12'}/bin:${env.PATH}"
  checkout scm
  sh 'sbt clean package test'
 }