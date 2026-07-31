package {package_name};

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("{被測類別} 單元測試")
class {被測類別}Test {

    // @Mock
    // private {DependencyType} dependency;

    @InjectMocks
    private {被測類別} sut; // System Under Test

    @BeforeEach
    void setUp() {
        // 初始化共用測試資料
    }

    @Nested
    @DisplayName("should_{預期行為}_When_{條件}")
    class {方法名稱}Tests {

        @Test
        @DisplayName("正常路徑：{預期行為}")
        void should_{預期行為}_When_{正常條件}() {
            // Arrange
            // 準備測試資料

            // Act
            // 呼叫被測方法

            // Assert
            // 驗證結果
            assertThat(/* actual */).isEqualTo(/* expected */);
        }

        @Test
        @DisplayName("邊界值：{邊界說明}")
        void should_{預期行為}_When_{邊界條件}() {
            // Arrange

            // Act

            // Assert
            assertThat(/* actual */).isNotNull();
        }

        @Test
        @DisplayName("異常路徑：{異常說明}")
        void should_throw{異常類型}_When_{異常條件}() {
            // Arrange

            // Act & Assert
            assertThatThrownBy(() -> sut.{方法名稱}(/* invalid args */))
                    .isInstanceOf({異常類型}.class)
                    .hasMessageContaining("{預期訊息}");
        }
    }
}
