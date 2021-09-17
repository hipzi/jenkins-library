#!/usr/bin/env groovy


def call(Map param){
	pipeline {
		agent {
			label "${param.agent}"
		}
		stages {
			stage ("telegram notif"){
				when {
					expression {
						return "$param.agent" == "dockerworker" || "$param.agent" == "zahra"
					}
				}
				steps{
					echo "${getMessage()} ${param.text}"
				}
			}
			stage('Build') {
				when {
					expression {
						return "$param.agent" == "dockerworker" || "$param.agent" == "zahra"
					}
				}
				steps {
					sh 'mvn -B -DskipTests clean package'
				}
			}
			stage('Test') {
				when {
					expression {
						return "$param.agent" == "dockerworker" || "$param.agent" == "zahra"
					}
				}
				steps {
					sh 'mvn test'
				}
				post {
					always {
						junit 'target/surefire-reports/*.xml'
					}
				}
			}
			stage('Build image') {
				when {
					expression {
						return "$param.agent" == "dockerworker"
					}
				}
				steps {
					sh 'docker build -t my-app . '
				}
			}
			stage('Run app container') {
				when {
					expression {
						return "$param.agent" == "dockerworker"
					}
				}
				steps {
					sh 'docker run -p 8181:8181 my-app'
				}
			}
			stage('Run app') {
				when {
					expression {
						return "$param.agent" == "zahra"
					}
				}
				steps {
					sh 'cd /home/hipzi/Jenkins-future-2'					
					sh 'java -jar my-app.jar'
				}
			}
		}

    }
}

def getMessage (){
	def commiter = sh(script: "git show -s --pretty=%cn",returnStdout: true).trim()
	def message = "$commiter deploying app"
	return message
}
