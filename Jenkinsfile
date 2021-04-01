pipeline {
  agent {
    label 'maven'
  }
  stages {
    stage('Build App') {
      steps {
        git branch: 'eap-7', url: 'http://gogs:3000/gogs/openshift-tasks.git'
        sh "mvn -s configuration/cicd-settings-nexus3.xml install -DskipTests=true"
      }
    }
    stage('Test') {
      steps {
        sh "mvn -s configuration/cicd-settings-nexus3.xml test"
        step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
      }
    }
    stage('Code Analysis') {
      steps {
        script {
          sh "mvn -s configuration/cicd-settings-nexus3.xml install sonar:sonar -Dsonar.host.url=http://sonarqube:9000 -Dsonar.login=5bc9cea4773892d896bf90df516311280f35d4e7 -DskipTests=true"
        }
      }
    }
    stage('Archive App') {
      steps {
        sh "mvn -s configuration/cicd-settings-nexus3.xml deploy -DskipTests=true -P nexus3"
      }
    }
    stage('Build Image') {
      steps {
        sh "cp target/openshift-tasks.war target/ROOT.war"
        script {
          openshift.withCluster() {
            openshift.withProject(env.DEV_PROJECT) {
              openshift.selector("bc", "tasks").startBuild("--from-file=target/ROOT.war", "--wait=true")
            }
          }
        }
      }
    }
    stage('Deploy DEV') {
      steps {
        script {
          openshift.withCluster() {
            openshift.withProject(env.DEV_PROJECT) {
              openshift.selector("dc", "tasks").rollout().latest();
            }
          }
        }
      }
    }
    stage('Acceptance Test') {
      agent {
        kubernetes {
          cloud 'openshift'
          /*yamlFile 'custom-maven.yaml'*/
          yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - args: ['$(JENKINS_SECRET)', '$(JENKINS_NAME)']
    image: openshift/jenkins-agent-maven-35-centos7
    name: jnlp
    tty: true
  - image: selenium/standalone-firefox
    name: webdriver
    tty: true
'''
        }
      }
      steps {
        dir('UAT') {
          sh "mvn -s ../configuration/cicd-settings-nexus3.xml test -Dselenide.browser=firefox -Dselenide.remote=http://localhost:4444/wd/hub/"
          step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
        }
      }
    }
    stage('Promote to STAGE?') {
      agent {
        label 'skopeo'
      }
      steps {
        timeout(time:15, unit:'MINUTES') {
            input message: "Promote to STAGE?", ok: "Promote"
        }

        script {
          openshift.withCluster() {
            if (env.ENABLE_QUAY.toBoolean()) {
              withCredentials([usernamePassword(credentialsId: "${openshift.project()}-quay-cicd-secret", usernameVariable: "QUAY_USER", passwordVariable: "QUAY_PWD")]) {
                sh "skopeo copy docker://quay.io/kshiraka/tasks-app:latest docker://quay.io/kshiraka/tasks-app:stage --src-creds \"$QUAY_USER:$QUAY_PWD\" --dest-creds \"$QUAY_USER:$QUAY_PWD\" --src-tls-verify=false --dest-tls-verify=false"
              }
            } else {
              openshift.tag("${env.DEV_PROJECT}/tasks:latest", "${env.STAGE_PROJECT}/tasks:stage")
            }
          }
        }
      }
    }
    stage('Deploy STAGE') {
      steps {
        script {
          openshift.withCluster() {
            openshift.withProject(env.STAGE_PROJECT) {
              openshift.selector("dc", "tasks").rollout().latest();
            }
          }
        }
      }
    }
  }
}