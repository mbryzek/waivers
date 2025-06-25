module Generated.IoBryzekWaiversApi exposing (..)

import Generated.ApiRequest as ApiRequest
import Iso8601
import Json.Decode as Decode
import Json.Decode.Pipeline as Pipeline
import Json.Encode as Encode
import Generated.ApiRequest exposing (ApiResult)
import Http exposing (Header)
import Time exposing (Posix)




encodeOptional : (a -> Encode.Value) -> Maybe a -> Encode.Value
encodeOptional encoder value =
    case value of
        Just v ->
            encoder v

        Nothing ->
            Encode.null


type alias HttpRequestParams =
     { apiHost: String
       , headers : List Header
     }


type ProjectStatus
    = ProjectStatusActive
    | ProjectStatusInactive
    | ProjectStatusUnknown

getAllProjectStatuses : List ProjectStatus
getAllProjectStatuses =
    [ ProjectStatusActive, ProjectStatusInactive ]

projectStatusToString : ProjectStatus -> String
projectStatusToString instance =
    case instance of
        ProjectStatusActive ->
            "active"

        ProjectStatusInactive ->
            "inactive"

        ProjectStatusUnknown ->
            "unknown"


projectStatusFromString : String -> ProjectStatus
projectStatusFromString value =
    if value == "active" then
        ProjectStatusActive

    else if value == "inactive" then
        ProjectStatusInactive

    else
        ProjectStatusUnknown

projectStatusEncoder : ProjectStatus -> Encode.Value
projectStatusEncoder instance =
    Encode.string (projectStatusToString instance)


projectStatusDecoder : Decode.Decoder ProjectStatus
projectStatusDecoder =
    Decode.map projectStatusFromString Decode.string


type SignatureStatus
    = SignatureStatusPending
    | SignatureStatusSigned
    | SignatureStatusExpired
    | SignatureStatusCancelled
    | SignatureStatusUnknown

getAllSignatureStatuses : List SignatureStatus
getAllSignatureStatuses =
    [ SignatureStatusPending, SignatureStatusSigned, SignatureStatusExpired, SignatureStatusCancelled ]

signatureStatusToString : SignatureStatus -> String
signatureStatusToString instance =
    case instance of
        SignatureStatusPending ->
            "pending"

        SignatureStatusSigned ->
            "signed"

        SignatureStatusExpired ->
            "expired"

        SignatureStatusCancelled ->
            "cancelled"

        SignatureStatusUnknown ->
            "unknown"


signatureStatusFromString : String -> SignatureStatus
signatureStatusFromString value =
    if value == "pending" then
        SignatureStatusPending

    else if value == "signed" then
        SignatureStatusSigned

    else if value == "expired" then
        SignatureStatusExpired

    else if value == "cancelled" then
        SignatureStatusCancelled

    else
        SignatureStatusUnknown

signatureStatusEncoder : SignatureStatus -> Encode.Value
signatureStatusEncoder instance =
    Encode.string (signatureStatusToString instance)


signatureStatusDecoder : Decode.Decoder SignatureStatus
signatureStatusDecoder =
    Decode.map signatureStatusFromString Decode.string


type WaiverStatus
    = WaiverStatusCurrent
    | WaiverStatusArchived
    | WaiverStatusUnknown

getAllWaiverStatuses : List WaiverStatus
getAllWaiverStatuses =
    [ WaiverStatusCurrent, WaiverStatusArchived ]

waiverStatusToString : WaiverStatus -> String
waiverStatusToString instance =
    case instance of
        WaiverStatusCurrent ->
            "current"

        WaiverStatusArchived ->
            "archived"

        WaiverStatusUnknown ->
            "unknown"


waiverStatusFromString : String -> WaiverStatus
waiverStatusFromString value =
    if value == "current" then
        WaiverStatusCurrent

    else if value == "archived" then
        WaiverStatusArchived

    else
        WaiverStatusUnknown

waiverStatusEncoder : WaiverStatus -> Encode.Value
waiverStatusEncoder instance =
    Encode.string (waiverStatusToString instance)


waiverStatusDecoder : Decode.Decoder WaiverStatus
waiverStatusDecoder =
    Decode.map waiverStatusFromString Decode.string


type alias Project =
  {
    id: String
    , name: String
    , slug: String
    , description: Maybe String
    , status: ProjectStatus
  }

projectEncoder : Project -> Encode.Value
projectEncoder instance =
        Encode.object
        [
            ( "id", Encode.string instance.id )
            , ( "name", Encode.string instance.name )
            , ( "slug", Encode.string instance.slug )
            , ( "description", encodeOptional Encode.string instance.description )
            , ( "status", projectStatusEncoder instance.status )

        ]


projectDecoder : Decode.Decoder Project
projectDecoder =
        Decode.succeed Project
            |> Pipeline.required "id" Decode.string
            |> Pipeline.required "name" Decode.string
            |> Pipeline.required "slug" Decode.string
            |> Pipeline.optional "description" (Decode.nullable Decode.string) Nothing
            |> Pipeline.required "status" projectStatusDecoder


type alias Signature =
  {
    id: String
    , user: User
    , waiver: Waiver
    , status: SignatureStatus
    , signedAt: Maybe Posix
    , signnowUrl: Maybe String
  }

signatureEncoder : Signature -> Encode.Value
signatureEncoder instance =
        Encode.object
        [
            ( "id", Encode.string instance.id )
            , ( "user", userEncoder instance.user )
            , ( "waiver", waiverEncoder instance.waiver )
            , ( "status", signatureStatusEncoder instance.status )
            , ( "signed_at", encodeOptional Iso8601.encode instance.signedAt )
            , ( "signnow_url", encodeOptional Encode.string instance.signnowUrl )

        ]


signatureDecoder : Decode.Decoder Signature
signatureDecoder =
        Decode.succeed Signature
            |> Pipeline.required "id" Decode.string
            |> Pipeline.required "user" userDecoder
            |> Pipeline.required "waiver" waiverDecoder
            |> Pipeline.required "status" signatureStatusDecoder
            |> Pipeline.optional "signed_at" (Decode.nullable Iso8601.decoder) Nothing
            |> Pipeline.optional "signnow_url" (Decode.nullable Decode.string) Nothing


type alias SignatureCompletion =
  {
    signatureData: String
  }

signatureCompletionEncoder : SignatureCompletion -> Encode.Value
signatureCompletionEncoder instance =
        Encode.object
        [
            ( "signature_data", Encode.string instance.signatureData )

        ]


signatureCompletionDecoder : Decode.Decoder SignatureCompletion
signatureCompletionDecoder =
        Decode.succeed SignatureCompletion
            |> Pipeline.required "signature_data" Decode.string


type alias User =
  {
    id: String
    , email: String
    , firstName: String
    , lastName: String
    , phone: Maybe String
  }

userEncoder : User -> Encode.Value
userEncoder instance =
        Encode.object
        [
            ( "id", Encode.string instance.id )
            , ( "email", Encode.string instance.email )
            , ( "first_name", Encode.string instance.firstName )
            , ( "last_name", Encode.string instance.lastName )
            , ( "phone", encodeOptional Encode.string instance.phone )

        ]


userDecoder : Decode.Decoder User
userDecoder =
        Decode.succeed User
            |> Pipeline.required "id" Decode.string
            |> Pipeline.required "email" Decode.string
            |> Pipeline.required "first_name" Decode.string
            |> Pipeline.required "last_name" Decode.string
            |> Pipeline.optional "phone" (Decode.nullable Decode.string) Nothing


type alias Waiver =
  {
    id: String
    , projectId: String
    , version: Int
    , title: String
    , content: String
    , status: WaiverStatus
  }

waiverEncoder : Waiver -> Encode.Value
waiverEncoder instance =
        Encode.object
        [
            ( "id", Encode.string instance.id )
            , ( "project_id", Encode.string instance.projectId )
            , ( "version", Encode.int instance.version )
            , ( "title", Encode.string instance.title )
            , ( "content", Encode.string instance.content )
            , ( "status", waiverStatusEncoder instance.status )

        ]


waiverDecoder : Decode.Decoder Waiver
waiverDecoder =
        Decode.succeed Waiver
            |> Pipeline.required "id" Decode.string
            |> Pipeline.required "project_id" Decode.string
            |> Pipeline.required "version" Decode.int
            |> Pipeline.required "title" Decode.string
            |> Pipeline.required "content" Decode.string
            |> Pipeline.required "status" waiverStatusDecoder


type alias WaiverForm =
  {
    firstName: String
    , lastName: String
    , email: String
    , phone: Maybe String
  }

waiverFormEncoder : WaiverForm -> Encode.Value
waiverFormEncoder instance =
        Encode.object
        [
            ( "first_name", Encode.string instance.firstName )
            , ( "last_name", Encode.string instance.lastName )
            , ( "email", Encode.string instance.email )
            , ( "phone", encodeOptional Encode.string instance.phone )

        ]


waiverFormDecoder : Decode.Decoder WaiverForm
waiverFormDecoder =
        Decode.succeed WaiverForm
            |> Pipeline.required "first_name" Decode.string
            |> Pipeline.required "last_name" Decode.string
            |> Pipeline.required "email" Decode.string
            |> Pipeline.optional "phone" (Decode.nullable Decode.string) Nothing


getProjectsProjectsBySlug : String -> (ApiResult Project -> msg) -> HttpRequestParams -> Cmd msg
getProjectsProjectsBySlug slug msg params =
    Http.request
        { method = "GET"
        , url = params.apiHost ++ "/projects/projects/" ++ slug
        , expect = ApiRequest.expectJson msg projectDecoder
        , headers = params.headers
        , timeout = Nothing
        , tracker = Nothing
        , body = Http.emptyBody
        }


postSignaturesProjectsSignaturesBySlug : String -> WaiverForm -> (ApiResult Signature -> msg) -> HttpRequestParams -> Cmd msg
postSignaturesProjectsSignaturesBySlug slug body msg params =
    Http.request
        { method = "POST"
        , url = params.apiHost ++ "/signatures/projects/" ++ slug ++ "/signatures"
        , expect = ApiRequest.expectJson msg signatureDecoder
        , headers = params.headers
        , timeout = Nothing
        , tracker = Nothing
        , body = Http.jsonBody (waiverFormEncoder body)
        }


getSignaturesSignaturesById : String -> (ApiResult Signature -> msg) -> HttpRequestParams -> Cmd msg
getSignaturesSignaturesById id msg params =
    Http.request
        { method = "GET"
        , url = params.apiHost ++ "/signatures/signatures/" ++ id
        , expect = ApiRequest.expectJson msg signatureDecoder
        , headers = params.headers
        , timeout = Nothing
        , tracker = Nothing
        , body = Http.emptyBody
        }


postSignaturesSignaturesCompleteById : String -> SignatureCompletion -> (ApiResult Signature -> msg) -> HttpRequestParams -> Cmd msg
postSignaturesSignaturesCompleteById id body msg params =
    Http.request
        { method = "POST"
        , url = params.apiHost ++ "/signatures/signatures/" ++ id ++ "/complete"
        , expect = ApiRequest.expectJson msg signatureDecoder
        , headers = params.headers
        , timeout = Nothing
        , tracker = Nothing
        , body = Http.jsonBody (signatureCompletionEncoder body)
        }


getWaiversProjectsWaiverBySlug : String -> (ApiResult Waiver -> msg) -> HttpRequestParams -> Cmd msg
getWaiversProjectsWaiverBySlug slug msg params =
    Http.request
        { method = "GET"
        , url = params.apiHost ++ "/waivers/projects/" ++ slug ++ "/waiver"
        , expect = ApiRequest.expectJson msg waiverDecoder
        , headers = params.headers
        , timeout = Nothing
        , tracker = Nothing
        , body = Http.emptyBody
        }