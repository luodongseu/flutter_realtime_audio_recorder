#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'realtime_audio_recorder'
  s.version          = '0.0.1'
  s.summary          = 'flutter_realtime_audio_recorder'
  s.description      = <<-DESC
flutter_realtime_audio_recorder
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Your Company' => 'email@example.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'

  s.ios.deployment_target = '10.0'
  
  s.ios.vendored_frameworks = 'Frameworks/lame.framework'
  s.vendored_frameworks = 'lame.framework'
end

