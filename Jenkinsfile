#!/usr/bin/env groovy
@Library('jenkins-pipeline-lib') _

node {
    def commitHash
    try {
        cleanWs()

          stage("checkout") {
                    appToken = github.generateAppToken()

                    sh "git init"
                    sh "git pull https://x-access-token:$appToken@github.com/navikt/eessi-pensjon-saksbehandling-api.git"
                    sh "make bump-version"

                    commitHash = sh(script: "git rev-parse HEAD", returnStdout: true).trim()
                    github.commitStatus("pending", "navikt/eessi-pensjon-saksbehandling-api", appToken, commitHash)
                }

        stage("build") {
            try {
                sh "make"
            } catch (e) {
                junit('build/test-results/test/**/*.xml')
                publishHTML([
                        allowMissing         : false,
                        alwaysLinkToLastBuild: false,
                        keepAll              : true,
                        reportDir            : 'build/reports/tests/test',
                        reportFiles          : 'index.html',
                        reportName           : 'HTML Report',
                        reportTitles         : ''
                ])

                throw e
            }
        }

        stage("release") {
            withCredentials([usernamePassword(credentialsId: 'nexusUploader', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                sh "docker login -u ${env.NEXUS_USERNAME} -p ${env.NEXUS_PASSWORD} repo.adeo.no:5443"
            }

            sh "make release"
            sh "git push --tags https://x-access-token:$appToken@github.com/navikt/eessi-pensjon-saksbehandling-api HEAD:master"
        }

        stage("upload manifest") {
            withCredentials([usernamePassword(credentialsId: 'nexusUploader', usernameVariable: 'NEXUS_USERNAME', passwordVariable: 'NEXUS_PASSWORD')]) {
                sh "make manifest"
            }
        }

        stage("deploy T8") {
            parallel (
                deploy: {
                    def version = sh(script: 'git describe --abbrev=0', returnStdout: true).trim()
                    build([
                        job       : 'nais-deploy-pipeline',
                        wait      : true,
                        parameters: [
                            string(name: 'APP', value: "eessi-pensjon-frontend-api-fss"),
                            string(name: 'REPO', value: "navikt/eessi-pensjon-saksbehandling-api"),
                            string(name: 'VERSION', value: "${version}"),
                            string(name: 'DEPLOY_REF', value: "${version}"),
                            string(name: 'DEPLOY_ENV', value: 't8'),
                            string(name: 'NAMESPACE', value: 't8'),
                            string(name: 'CLUSTER', value: 'fss'),
                            string(name: 'CONTEXT_ROOTS', value: '/callback')
                        ]
                    ])
                }
            )
        }

        stage("Cucumber tests") {
/*                 build([
                     job       : 'Automatiske tester',
                     wait      : true,
                     parameters: [
                         string(name: 'environment', value: "autotest_env_T"),
                         booleanParam(name: 'testsaksbehandler', value: false),
                         booleanParam(name: 'testBorger', value: true),
                         booleanParam(name: 'testKravAlder', value: false)
                     ]
                  ])
*/                  
        }

          stage("deploy Q1") {
                   parallel(
                       deploy_to_fss: {
                           def version = sh(script: 'git describe --abbrev=0', returnStdout: true).trim()
                           build([
                                   job       : 'nais-deploy-pipeline',
                                   wait      : true,
                                   parameters: [
                                           string(name: 'APP', value: "eessi-pensjon-frontend-api-fss"),
                                           string(name: 'REPO', value: "navikt/eessi-pensjon-saksbehandling-api"),
                                           string(name: 'VERSION', value: "${version}"),
                                           string(name: 'DEPLOY_REF', value: "${version}"),
                                           string(name: 'DEPLOY_ENV', value: 'q1'),
                                           string(name: 'NAMESPACE', value: 'q1'),
                                           string(name: 'CLUSTER', value: 'fss'),
                                           string(name: 'CONTEXT_ROOTS', value: '/callback')
                                   ]
                           ])
                       }
                   )
               }
               stage("deploy Q2") {
                   parallel(
                       deploy_to_fss: {
                           def version = sh(script: 'git describe --abbrev=0', returnStdout: true).trim()
                           build([
                                   job       : 'nais-deploy-pipeline',
                                   wait      : true,
                                   parameters: [
                                           string(name: 'APP', value: "eessi-pensjon-frontend-api-fss"),
                                           string(name: 'REPO', value: "navikt/eessi-pensjon-saksbehandling-api"),
                                           string(name: 'VERSION', value: "${version}"),
                                           string(name: 'DEPLOY_REF', value: "${version}"),
                                           string(name: 'DEPLOY_ENV', value: 'q2'),
                                           string(name: 'NAMESPACE', value: 'q2'),
                                           string(name: 'CLUSTER', value: 'fss'),
                                           string(name: 'CONTEXT_ROOTS', value: '/callback')
                                   ]
                           ])
                       }
                   )
               }


        github.commitStatus("success", "navikt/eessi-pensjon-saksbehandling-api", appToken, commitHash)
    } catch (err) {
        github.commitStatus("failure", "navikt/eessi-pensjon-saksbehandling-api", appToken, commitHash)
        throw err
    }
}
