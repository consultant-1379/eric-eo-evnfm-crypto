{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-eo-evnfm-crypto.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "eric-eo-evnfm-crypto.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- template "eric-eo-evnfm-crypto.name" . -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-eo-evnfm-crypto.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart version as used by the chart label.
*/}}
{{- define "eric-eo-evnfm-crypto.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create main image registry url
*/}}
{{- define "eric-eo-evnfm-crypto.mainImagePath" -}}
    {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
    {{- $registryUrl := $productInfo.images.cryptoService.registry -}}
    {{- $repoPath := $productInfo.images.cryptoService.repoPath -}}
    {{- $name := $productInfo.images.cryptoService.name -}}
    {{- $tag := $productInfo.images.cryptoService.tag -}}
    {{- if .Values.global -}}
      {{- if .Values.global.registry -}}
          {{- if .Values.global.registry.url -}}
              {{- $registryUrl = .Values.global.registry.url -}}
          {{- end -}}
      {{- end -}}
    {{- end -}}
    {{- if .Values.imageCredentials -}}
      {{- if .Values.imageCredentials.cryptoService -}}
          {{- if .Values.imageCredentials.cryptoService.registry -}}
              {{- if .Values.imageCredentials.cryptoService.registry.url -}}
                  {{- $registryUrl = .Values.imageCredentials.cryptoService.registry.url -}}
              {{- end -}}
          {{- end -}}
          {{- if not (kindIs "invalid" .Values.imageCredentials.cryptoService.repoPath) -}}
              {{- $repoPath = .Values.imageCredentials.cryptoService.repoPath -}}
          {{- else if not (kindIs "invalid" .Values.global.registry.repoPath) }}
              {{- $repoPath = .Values.global.registry.repoPath -}}
          {{- end -}}
      {{- end -}}
      {{- if not (kindIs "invalid" .Values.imageCredentials.repoPath) -}}
          {{- $repoPath = .Values.imageCredentials.repoPath -}}
      {{- end -}}
    {{- end -}}
    {{- if $repoPath -}}
      {{- $repoPath = printf "%s/" $repoPath -}}
    {{- end -}}
    {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{/*
Create volume permissions image registry url
*/}}
{{- define "eric-eo-evnfm-crypto.volumePermissionsImagePath" -}}
    {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
    {{- $registryUrl := $productInfo.images.volumePermissions.registry -}}
    {{- $repoPath := $productInfo.images.volumePermissions.repoPath -}}
    {{- $name := $productInfo.images.volumePermissions.name -}}
    {{- $tag := $productInfo.images.volumePermissions.tag -}}
    {{- if .Values.global -}}
        {{- if .Values.global.registry -}}
            {{- if .Values.global.registry.url -}}
                {{- $registryUrl = .Values.global.registry.url -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if .Values.imageCredentials -}}
        {{- if .Values.imageCredentials.volumePermissions -}}
            {{- if .Values.imageCredentials.volumePermissions.registry -}}
                {{- if .Values.imageCredentials.volumePermissions.registry.url -}}
                    {{- $registryUrl = .Values.imageCredentials.volumePermissions.registry.url -}}
                {{- end -}}
            {{- end -}}
            {{- if not (kindIs "invalid" .Values.imageCredentials.volumePermissions.repoPath) -}}
                {{- $repoPath = .Values.imageCredentials.volumePermissions.repoPath -}}
            {{- end -}}
        {{- end -}}
        {{- if not (kindIs "invalid" .Values.imageCredentials.repoPath) -}}
            {{- $repoPath = .Values.imageCredentials.repoPath -}}
        {{- end -}}
    {{- end -}}
    {{- if $repoPath -}}
        {{- $repoPath = printf "%s/" $repoPath -}}
    {{- end -}}
    {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{/*
Create Ericsson Product Info
*/}}
{{- define "eric-eo-evnfm-crypto.helm-annotations" -}}
  {{- include "eric-eo-evnfm-library-chart.helm-annotations" . }}
{{- end}}

{{/*
Create prometheus info
*/}}
{{- define "eric-eo-evnfm-crypto.prometheus" -}}
  {{- include "eric-eo-evnfm-library-chart.prometheus" . -}}
{{- end -}}

{{/*
Create Ericsson product app.kubernetes.io info
*/}}
{{- define "eric-eo-evnfm-crypto.kubernetes-io-info" -}}
  {{- include "eric-eo-evnfm-library-chart.kubernetes-io-info" .}}
{{- end -}}

{{/*
Create image pull secrets
*/}}
{{- define "eric-eo-evnfm-crypto.pullSecrets" -}}
  {{- include "eric-eo-evnfm-library-chart.pullSecrets" . }}
{{- end -}}

{{/*
Create pullPolicy for crypto service container
*/}}
{{- define "eric-eo-evnfm-crypto.imagePullPolicy" -}}
  {{- include "eric-eo-evnfm-library-chart.imagePullPolicy" (dict "ctx" . "svcRegistryName" "cryptoService") -}}
{{- end -}}

{{/*
Create pullPolicy for init container
*/}}
{{- define "eric-eo-evnfm-crypto.sles.imagePullPolicy" -}}
  {{- include "eric-eo-evnfm-library-chart.imagePullPolicy" (dict "ctx" . "svcRegistryName" "sles") -}}
{{- end -}}

{/*
Define probes property
*/}}
{{- define "eric-eo-evnfm-crypto.probes" -}}
{{- $default := .Values.probes -}}
{{- if .Values.probing }}
  {{- if .Values.probing.liveness }}
    {{- if .Values.probing.liveness.cryptoService }}
      {{- $default := mergeOverwrite $default.cryptoService.livenessProbe .Values.probing.liveness.cryptoService  -}}
    {{- end }}
  {{- end }}
  {{- if .Values.probing.readiness }}
    {{- if .Values.probing.readiness.cryptoService }}
      {{- $default := mergeOverwrite $default.cryptoService.readinessProbe .Values.probing.readiness.cryptoService  -}}
    {{- end }}
  {{- end }}
{{- end }}
{{- $default | toJson -}}
{{- end -}}

{{/*
To support Dual stack.
*/}}
{{- define "eric-eo-evnfm-crypto.internalIPFamily" -}}
  {{- include "eric-eo-evnfm-library-chart.internalIPFamily" . }}
{{- end -}}

{{/*
Check global.security.tls.enabled
*/}}
{{- define "eric-eo-evnfm-crypto.global-security-tls-enabled" -}}
  {{- include "eric-eo-evnfm-library-chart.global-security-tls-enabled" . -}}
{{- end -}}

{{/*
DR-D470217-007-AD
This helper defines whether this service enter the Service Mesh or not.
*/}}
{{- define "eric-eo-evnfm-crypto.service-mesh-enabled" -}}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-enabled" . -}}
{{- end -}}

{{/*
Define podPriority property
*/}}
{{- define "eric-eo-evnfm-crypto.podPriority" -}}
  {{- include "eric-eo-evnfm-library-chart.podPriority" ( dict "ctx" . "svcName" "cryptoService" ) -}}
{{- end -}}

{{/*
DR-D470217-011 This helper defines the annotation which bring the service into the mesh.
*/}}
{{- define "eric-eo-evnfm-crypto.service-mesh-inject" -}}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-inject" . -}}
{{- end -}}

{{/*
GL-D470217-080-AD
This helper captures the service mesh version from the integration chart to
annotate the workloads so they are redeployed in case of service mesh upgrade.
*/}}
{{- define "eric-eo-evnfm-crypto.service-mesh-version" -}}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-version" . -}}
{{- end -}}

{{/*
This helper defines log level for Service Mesh.
*/}}
{{- define "eric-eo-evnfm-crypto.service-mesh-logs" -}}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-logs" . -}}
{{- end -}}

{{/*
Istio excludeOutboundPorts. Outbound ports to be excluded from redirection to Envoy.
*/}}
{{- define "eric-eo-evnfm-crypto.excludeOutboundPorts" -}}
  {{- include "eric-eo-evnfm-library-chart.excludeOutboundPorts" . -}}
{{- end -}}

{{/*
DR-D1123-124
Evaluating the Security Policy Cluster Role Name
*/}}
{{- define "eric-eo-evnfm-crypto.securityPolicy.reference" -}}
  {{- include "eric-eo-evnfm-library-chart.securityPolicy.reference" . }}
{{- end -}}

{{/*
Create Ericsson product specific annotations
*/}}
{{- define "eric-eo-evnfm-crypto.helm-annotations_product_name" -}}
  {{ include "eric-eo-evnfm-library-chart.helm-annotations_product_name" .}}
{{- end -}}
{{- define "eric-eo-evnfm-crypto.helm-annotations_product_number" -}}
  {{ include "eric-eo-evnfm-library-chart.helm-annotations_product_number" .}}
{{- end -}}
{{- define "eric-eo-evnfm-crypto.helm-annotations_product_revision" -}}
  {{ include "eric-eo-evnfm-library-chart.helm-annotations_product_revision" .}}
{{- end -}}

{{/*
Create a dict of annotations for the product information (DR-D1121-064, DR-D1121-067).
*/}}
{{- define "eric-eo-evnfm-crypto.product-info" }}
ericsson.com/product-name: {{ template "eric-eo-evnfm-crypto.helm-annotations_product_name" . }}
ericsson.com/product-number: {{ template "eric-eo-evnfm-crypto.helm-annotations_product_number" . }}
ericsson.com/product-revision: {{ template "eric-eo-evnfm-crypto.helm-annotations_product_revision" . }}
{{- end }}

{{/*
Common annotations
*/}}
{{- define "eric-eo-evnfm-crypto.annotations" -}}
  {{- $productInfo := include "eric-eo-evnfm-crypto.helm-annotations" . | fromYaml -}}
  {{- $globalAnn := (.Values.global).annotations -}}
  {{- $serviceAnn := .Values.annotations -}}
  {{- include "eric-eo-evnfm-library-chart.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $globalAnn $serviceAnn)) | trim }}
{{- end -}}

{{/*
Kubernetes labels
*/}}
{{- define "eric-eo-evnfm-crypto.kubernetes-labels" -}}
app.kubernetes.io/name: {{ include "eric-eo-evnfm-crypto.name" . }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
app.kubernetes.io/version: {{ include "eric-eo-evnfm-crypto.version" . }}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "eric-eo-evnfm-crypto.labels" -}}
  {{- $kubernetesLabels := include "eric-eo-evnfm-crypto.kubernetes-labels" . | fromYaml -}}
  {{- $globalLabels := (.Values.global).labels -}}
  {{- $serviceLabels := .Values.labels -}}
  {{- include "eric-eo-evnfm-library-chart.mergeLabels" (dict "location" .Template.Name "sources" (list $kubernetesLabels $globalLabels $serviceLabels)) }}
{{- end -}}

{{/*
Merged labels for extended defaults
*/}}
{{- define "eric-eo-evnfm-crypto.labels.extended-defaults" -}}
  {{- $extendedLabels := dict -}}
  {{- $_ := set $extendedLabels "logger-communication-type" "direct" -}}
  {{- $_ := set $extendedLabels "eric-sec-key-management-access" "true" -}}
  {{- $_ := set $extendedLabels "app" (include "eric-eo-evnfm-crypto.name" .) -}}
  {{- $_ := set $extendedLabels "chart" (include "eric-eo-evnfm-crypto.chart" .) -}}
  {{- $_ := set $extendedLabels "release" (.Release.Name) -}}
  {{- $_ := set $extendedLabels "heritage" (.Release.Service) -}}
  {{- $commonLabels := include "eric-eo-evnfm-crypto.labels" . | fromYaml -}}
  {{- $serviceMesh := include "eric-eo-evnfm-crypto.service-mesh-inject" . | fromYaml -}}
  {{- include "eric-eo-evnfm-library-chart.mergeLabels" (dict "location" .Template.Name "sources" (list $commonLabels $extendedLabels $serviceMesh)) | trim }}
{{- end -}}

{{/*
Define tolerations property
Change tolerationSeconds to 300 for HA setup
*/}}
{{- define "eric-eo-evnfm-crypto.tolerations.cryptoService" -}}
  {{- include "eric-eo-evnfm-library-chart.merge-tolerations" (dict "root" . "podbasename" "cryptoService" ) -}}
{{- end -}}

{{/*
Define nodeSelector property
*/}}
{{- define "eric-eo-evnfm-crypto.nodeSelector" -}}
  {{- include "eric-eo-evnfm-library-chart.nodeSelector" . -}}
{{- end -}}

{{/*
KMS CA certificate file for TLS communication to ADP KMS.
*/}}
{{- define "eric-eo-evnfm-crypto.kmsCaCertFile" -}}
"/run/secrets/kms-ca-cert/cacertbundle.pem"
{{- end -}}

{{- define "eric-eo-evnfm-crypto.migrationImagePath" -}}
    {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
    {{- $registryUrl := $productInfo.images.migration.registry -}}
    {{- $repoPath := $productInfo.images.migration.repoPath -}}
    {{- $name := $productInfo.images.migration.name -}}
    {{- $tag := $productInfo.images.migration.tag -}}
    {{- if .Values.global -}}
            {{- if .Values.global.registry -}}
                {{- if .Values.global.registry.url -}}
                    {{- $registryUrl = .Values.global.registry.url -}}
                {{- end -}}
            {{- end -}}
        {{- end -}}
        {{- if .Values.imageCredentials -}}
            {{- if .Values.imageCredentials.migration -}}
                {{- if .Values.imageCredentials.migration.registry -}}
                    {{- if .Values.imageCredentials.migration.registry.url -}}
                        {{- $registryUrl = .Values.imageCredentials.migration.registry.url -}}
                    {{- end -}}
                {{- end -}}
                {{- if not (kindIs "invalid" .Values.imageCredentials.migration.repoPath) -}}
                    {{- $repoPath = .Values.imageCredentials.migration.repoPath -}}
                {{- else if not (kindIs "invalid" .Values.global.registry.repoPath) }}
                    {{- $repoPath = .Values.global.registry.repoPath -}}
                {{- end -}}
            {{- end -}}
            {{- if not (kindIs "invalid" .Values.imageCredentials.repoPath) -}}
                {{- $repoPath = .Values.imageCredentials.repoPath -}}
            {{- end -}}
        {{- end -}}
        {{- if $repoPath -}}
            {{- $repoPath = printf "%s/" $repoPath -}}
        {{- end -}}
    {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{- define "eric-eo-evnfm-crypto.cleanupImagePath" -}}
    {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
    {{- $registryUrl := $productInfo.images.cleanup.registry -}}
    {{- $repoPath := $productInfo.images.cleanup.repoPath -}}
    {{- $name := $productInfo.images.cleanup.name -}}
    {{- $tag := $productInfo.images.cleanup.tag -}}
    {{- if .Values.global -}}
            {{- if .Values.global.registry -}}
                {{- if .Values.global.registry.url -}}
                    {{- $registryUrl = .Values.global.registry.url -}}
                {{- end -}}
            {{- end -}}
        {{- end -}}
        {{- if .Values.imageCredentials -}}
            {{- if .Values.imageCredentials.cleanup -}}
                {{- if .Values.imageCredentials.cleanup.registry -}}
                    {{- if .Values.imageCredentials.cleanup.registry.url -}}
                        {{- $registryUrl = .Values.imageCredentials.cleanup.registry.url -}}
                    {{- end -}}
                {{- end -}}
                {{- if not (kindIs "invalid" .Values.imageCredentials.cleanup.repoPath) -}}
                    {{- $repoPath = .Values.imageCredentials.cleanup.repoPath -}}
                {{- else if not (kindIs "invalid" .Values.global.registry.repoPath) }}
                    {{- $repoPath = .Values.global.registry.repoPath -}}
                {{- end -}}
            {{- end -}}
            {{- if not (kindIs "invalid" .Values.imageCredentials.repoPath) -}}
                {{- $repoPath = .Values.imageCredentials.repoPath -}}
            {{- end -}}
        {{- end -}}
        {{- if $repoPath -}}
            {{- $repoPath = printf "%s/" $repoPath -}}
        {{- end -}}
    {{- printf "%s/%s%s:%s" $registryUrl $repoPath $name $tag -}}
{{- end -}}

{{/*
Create fsGroup Values DR-1123-136
*/}}
{{- define "eric-eo-evnfm-crypto.fsGroup" -}}
  {{- include "eric-eo-evnfm-library-chart.fsGroup" . -}}
{{- end -}}

{{/*
DR-D470222-010
Configuration of Log Collection Streaming Method
*/}}
{{- define "eric-eo-evnfm-crypto.log.streamingMethod" -}}
  {{- include "eric-eo-evnfm-library-chart.log.streamingMethod" . -}}
{{- end }}

{{/*
Define ServiceAccount template
*/}}
{{- define "eric-eo-evnfm-crypto.serviceAccount.name" -}}
  {{- printf "%s-sa" (include "eric-eo-evnfm-crypto.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
DR-D1123-133
Standardized secret file names for certificates.
*/}}
{{- define "eric-eo-evnfm-crypto.trustedInternalRootCa" -}}
  {{- include "tls.secret.trustedInternalRootCa" . -}}
{{- end -}}

{{/*
DR-D1123-134
Rolekind parameter for generation of role bindings for admission control in OpenShift environment
*/}}
{{- define "eric-eo-evnfm-crypto.securityPolicy.rolekind" -}}
  {{- include "eric-eo-evnfm-library-chart.securityPolicy.rolekind" . -}}
{{- end -}}

{{/*
DR-D1123-134
Rolename parameter for generation of role bindings for admission control in OpenShift environment
*/}}
{{- define "eric-eo-evnfm-crypto.securityPolicy.rolename" -}}
  {{- include "eric-eo-evnfm-library-chart.securityPolicy.rolename" . -}}
{{- end -}}

{{/*
DR-D1123-134
RoleBinding name for generation of role bindings for admission control in OpenShift environment
*/}}
{{- define "eric-eo-evnfm-crypto.securityPolicy.rolebinding.name" -}}
  {{- include "eric-eo-evnfm-library-chart.securityPolicy.rolebinding.name" . -}}
{{- end -}}